/*
 * Copyright 2021 Usman Saleem.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package web3signer.configuration.generator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.bls.BLSPublicKey;

public class HashicorpVaultClient {
  private static final Logger LOG = LoggerFactory.getLogger(HashicorpVaultClient.class);

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final URI hashicorpApiEndpoint;
  private final String token;
  private final URI hashicorpInitEndpoint;

  public HashicorpVaultClient(final URI hashicorpApiEndpoint, final String token) {
    this.hashicorpApiEndpoint = hashicorpApiEndpoint;
    this.token = token;
    hashicorpInitEndpoint =
        URI.create(
            String.format(
                "%s://%s%s",
                hashicorpApiEndpoint.getScheme(),
                hashicorpApiEndpoint.getAuthority(),
                "/v1/sys/init"));
  }

  public boolean isInitialized() {
    LOG.info("Checking Hashicorp Vault status {}", hashicorpInitEndpoint);
    final HttpRequest httpRequestInit = HttpRequest.newBuilder(hashicorpInitEndpoint).GET().build();

    try {
      final HttpResponse<String> httpResponseInit =
          httpClient.send(httpRequestInit, HttpResponse.BodyHandlers.ofString());
      if (httpResponseInit.statusCode() == 200) {
        return true;
      }
      LOG.debug("Status Code: {}", httpResponseInit.statusCode());
      LOG.debug("Result: {}", httpResponseInit.body());
    } catch (IOException | InterruptedException e) {
      LOG.error("Error getting init status from Hashicorp ");
    }
    return false;
  }

  public List<BLSPublicKey> insertSecret(final Set<BLSKeyPair> blsKeys) {
    final AtomicInteger count = new AtomicInteger(0);
    List<BLSPublicKey> blsPublicKeys =
        blsKeys.parallelStream()
            .map(
                blsKeyPair -> {
                  System.out.printf("\rInserting key in vault: %d ...", count.incrementAndGet());
                  final BLSPublicKey publicKey = blsKeyPair.getPublicKey();
                  final String publicKeyHex = publicKey.toBytesCompressed().toUnprefixedHexString();

                  final String privateKeyHex =
                      blsKeyPair.getSecretKey().toBytes().toUnprefixedHexString();
                  final URI postURI =
                      URI.create(hashicorpApiEndpoint.toString() + "/data/" + publicKeyHex)
                          .normalize();
                  LOG.debug("Submitting to {}", postURI);

                  final HttpRequest httpRequestPost = buildHttpRequest(privateKeyHex, postURI);
                  try {

                    final HttpResponse<String> response =
                        httpClient.send(httpRequestPost, HttpResponse.BodyHandlers.ofString());
                    final int statusCode = response.statusCode();
                    if (statusCode == 200) {
                      return publicKey;
                    } else {
                      LOG.warn(
                          "Invalid status code from Hashicorp for {}: {}", postURI, statusCode);
                      LOG.warn(response.body());
                      return null;
                    }

                  } catch (IOException | InterruptedException e) {
                    LOG.error("Posting secret to {} failed: {}", postURI, e.getMessage());
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    System.out.println("\nData inserted in vault.");
    return blsPublicKeys;
  }

  private HttpRequest buildHttpRequest(final String privateKeyHex, final URI postURI) {
    return HttpRequest.newBuilder(postURI)
        .header("X-Vault-Token", token)
        .header("Content-Type", "application/json")
        .POST(
            HttpRequest.BodyPublishers.ofString(
                String.format("{\"data\":{\"value\":\"%s\"} }", privateKeyHex)))
        .build();
  }
}
