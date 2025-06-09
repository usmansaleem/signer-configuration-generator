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

import static tech.pegasys.teku.bls.keystore.model.Pbkdf2PseudoRandomFunction.HMAC_SHA256;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.bls.keystore.KeyStore;
import tech.pegasys.teku.bls.keystore.KeyStoreLoader;
import tech.pegasys.teku.bls.keystore.model.Cipher;
import tech.pegasys.teku.bls.keystore.model.CipherFunction;
import tech.pegasys.teku.bls.keystore.model.KdfParam;
import tech.pegasys.teku.bls.keystore.model.KeyStoreData;
import tech.pegasys.teku.bls.keystore.model.Pbkdf2Param;

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
    final AtomicInteger count = new AtomicInteger(0);
    blsPublicKeys.parallelStream()
        .forEach(
            blsPublicKey -> {
              System.out.printf("\rCreating configuration file: %d ...", count.incrementAndGet());
              final String publicKey = blsPublicKey.toBytesCompressed().toUnprefixedHexString();
              final URI secretsEndpoint =
                  URI.create(hashicorpApiEndpoint.toString() + "/data/" + publicKey).normalize();
              final String content =
                  getHashicorpYamlConfiguration(
                      secretsEndpoint, token, tlsKnownHosts, overrideVaultHost);
              var outputFileName = BLSKeyGenerator.secureRandomString();
              var outputFile = outputDir.resolve(outputFileName + ".yaml");
              try {
                Files.writeString(outputFile, content);
              } catch (IOException e) {
                LOG.error("Error creating configuration file {}: {}", outputFile, e.getMessage());
              }
            });
    System.out.println("\nConfiguration files created.");
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
          var outputFileName = BLSKeyGenerator.secureRandomString();
          var outputFile = outputDir.resolve(outputFileName + ".yaml");
          try {
            Files.writeString(outputFile, content);
          } catch (IOException e) {
            LOG.error("Error creating configuration file {}: {}", outputFile, e.getMessage());
          }
        });
  }

  public void createKeystoreConfigurationFiles(
      final Set<BLSKeyPair> blsKeyPairs,
      final boolean generateConfig,
      final Path keystoreDirInConfig,
      final int kdfCounter) {
    // create password file first
    try {
      var passwordFile = outputDir.resolve("password.txt");
      Files.writeString(passwordFile, "password");
      LOG.info("Created password file in {}", passwordFile);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    if (!generateConfig) {
      LOG.info("Skipping configuration file generation");
    }

    // create encrypted keystore files and configuration files
    final AtomicInteger count = new AtomicInteger(0);
    blsKeyPairs.parallelStream()
        .forEach(
            blsKeyPair -> {
              var outputFileName = BLSKeyGenerator.secureRandomString();
              var keystoreFileName = outputFileName + ".json";
              var configFileName = outputFileName + ".yaml";

              // generate keystore file
              try {
                createKeyStoreFile(
                    blsKeyPair.getSecretKey().toBytes(),
                    blsKeyPair.getPublicKey().toBytesCompressed(),
                    "password",
                    outputDir.resolve(keystoreFileName),
                    kdfCounter);
              } catch (final IOException e) {
                LOG.error(
                    "Unable to create keystore file: {}. Error: {}",
                    keystoreFileName,
                    e.getMessage());
                return;
              }

              count.incrementAndGet();

              if (!generateConfig) {
                return;
              }

              // create configuration file
              var configFileMap =
                  Map.of(
                      "type",
                      "file-keystore",
                      "keyType",
                      "BLS",
                      "keystoreFile",
                      Optional.ofNullable(keystoreDirInConfig)
                          .orElse(outputDir)
                          .resolve(keystoreFileName)
                          .toString(), // json file
                      "keystorePasswordFile",
                      Optional.ofNullable(keystoreDirInConfig)
                          .orElse(outputDir)
                          .resolve("password.txt")
                          .toString()); // password file
              var content = new Yaml(DUMPER_OPTIONS).dump(configFileMap);
              try {
                Files.writeString(outputDir.resolve(configFileName), content);
              } catch (IOException e) {
                LOG.error(
                    "Error creating configuration file {}: {}", configFileName, e.getMessage());
              }
            });

    LOG.info("Created {} keystore/configuration files in {}", count.get(), outputDir);
  }

  private void createKeyStoreFile(
      final Bytes privateKey,
      final Bytes publicKey,
      final String password,
      final Path keyStoreFilePath,
      final int kdfCounter)
      throws IOException {
    final Bytes salt = Bytes.random(32, BLSKeyGenerator.getSecureRandom());
    final Bytes iv = Bytes.random(16, BLSKeyGenerator.getSecureRandom());
    // final int counter = 65536; // 2^16
    final KdfParam kdfParam = new Pbkdf2Param(32, kdfCounter, HMAC_SHA256, salt);
    final Cipher cipher = new Cipher(CipherFunction.AES_128_CTR, iv);
    final KeyStoreData keyStoreData =
        KeyStore.encrypt(privateKey, publicKey, password, "", kdfParam, cipher);
    KeyStoreLoader.saveToFile(keyStoreFilePath, keyStoreData);
  }

  private String getHashicorpYamlConfiguration(
      final URI uri, final String token, final Path tlsKnownHosts, final String overrideVaultHost) {
    // create configuration file
    final Map<String, Object> map = new HashMap<>();
    map.put("type", "hashicorp");
    map.put("keyPath", uri.getPath());
    map.put("keyName", "value");
    map.put(
        "serverHost",
        overrideVaultHost == null || overrideVaultHost.isBlank()
            ? uri.getHost()
            : overrideVaultHost);
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
