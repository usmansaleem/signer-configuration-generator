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

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.bls.BLSPublicKey;

public class Web3SignerYamlConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(Web3SignerYamlConfiguration.class);
  private final Path outputDir;
  private static final DumperOptions DUMPER_OPTIONS = new DumperOptions();

  static {
    DUMPER_OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
  }

  public Web3SignerYamlConfiguration(final Path outputDir) {
    this.outputDir = outputDir;
    LOG.debug("Creating output directory: {}", outputDir);
    try {
      Files.createDirectories(outputDir);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void createHashicorpYamlConfigurationFiles(
      final List<BLSPublicKey> blsPublicKeys,
      final URI hashicorpApiEndpoint,
      final String token,
      final Path tlsKnownHosts,
      final String overrideVaultHost) {
    blsPublicKeys.parallelStream()
        .forEach(
            blsPublicKey -> {
              final String publicKey = blsPublicKey.toBytesCompressed().toUnprefixedHexString();
              final String abbrPublicKey = blsPublicKey.toAbbreviatedString();
              final URI secretsEndpoint =
                  URI.create(hashicorpApiEndpoint.toString() + "/data/" + publicKey).normalize();
              final String content =
                  getHashicorpYamlConfiguration(
                      secretsEndpoint, token, tlsKnownHosts, overrideVaultHost);
              Path outputFile = outputDir.resolve(abbrPublicKey + ".yaml");
              int counter = 1;
              while (Files.exists(outputFile)) {
                outputFile = outputDir.resolve(abbrPublicKey + "-" + counter + ".yaml");
                counter++;
              }
              try {
                Files.writeString(outputFile, content);
              } catch (IOException e) {
                LOG.error("Error creating configuration file {}: {}", outputFile, e.getMessage());
              }
            });
  }

  public void createRawYamlConfigurationFiles(final Set<BLSKeyPair> blsKeyPairs) {
    // create configuration file
    blsKeyPairs.forEach(
        blsKeyPair -> {
          final Map<String, String> map =
              Map.of(
                  "type",
                  "file-raw",
                  "privateKey",
                  blsKeyPair.getSecretKey().toBytes().toHexString());
          final String content = new Yaml(DUMPER_OPTIONS).dump(map);
          final String publicKey = blsKeyPair.getPublicKey().toAbbreviatedString();
          Path outputFile = outputDir.resolve(publicKey + ".yaml");
          int counter = 1;
          while (Files.exists(outputFile)) {
            outputFile = outputDir.resolve(publicKey + "-" + counter + ".yaml");
            counter++;
          }
          try {
            Files.writeString(outputFile, content);
          } catch (IOException e) {
            LOG.error("Error creating configuration file {}: {}", outputFile, e.getMessage());
          }
        });
  }

  private String getHashicorpYamlConfiguration(
      final URI uri, final String token, final Path tlsKnownHosts, final String overrideVaultHost) {
    // create configuration file
    final Map<String, Object> map = new HashMap<>();
    map.put("type", "hashicorp");
    map.put("keyPath", uri.getPath());
    map.put("keyName", "value");
    map.put(
        "serverHost", Strings.isNullOrEmpty(overrideVaultHost) ? uri.getHost() : overrideVaultHost);
    map.put("serverPort", uri.getPort());
    map.put("token", token);

    if ("https".equalsIgnoreCase(uri.getScheme())) {
      map.put("tlsEnabled", "true");
      map.put("tlsKnownServersPath", tlsKnownHosts.toString());
    } else {
      map.put("tlsEnabled", "false");
    }

    return new Yaml(DUMPER_OPTIONS).dump(map);
  }
}
