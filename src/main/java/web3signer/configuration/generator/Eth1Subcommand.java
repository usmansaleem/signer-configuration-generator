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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import picocli.CommandLine;
import web3signer.configuration.util.TomlStringBuilder;

@CommandLine.Command(name = "ethsigner")
public class Eth1Subcommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(HashicorpSubcommand.class);

  @CommandLine.Option(
      names = "--output",
      description = "Output directory for EthSigner configuration files. Default: ${DEFAULT-VALUE}")
  Path outputDir = Path.of("./keys");

  @CommandLine.Option(
      names = "--count",
      converter = PositiveIntegerConverter.class,
      description = "Number of keys to generate and insert. Default: ${DEFAULT-VALUE}")
  int count = 5;

  @CommandLine.Option(
      names = "--password",
      description = "Password for the encrypted v3 keystore. Default: ${DEFAULT-VALUE}")
  String password = "password";

  @Override
  public Integer call() throws Exception {
    LOG.info("Creating output directory: {}", outputDir);
    final Path outputDirCreated = Files.createDirectories(outputDir).toAbsolutePath();

    LOG.info("Generating {} SECP V3 Keystore", count);
    for (int i = 0; i < count; i++) {
      final ECKeyPair ecKeyPair = Keys.createEcKeyPair();
      final String address = Keys.getAddress(ecKeyPair);
      final String fileName =
          WalletUtils.generateWalletFile(password, ecKeyPair, outputDir.toFile(), true);

      final Path keyFile = outputDirCreated.resolve(fileName).toAbsolutePath();
      final Path passwordFile = outputDirCreated.resolve(address + ".password").toAbsolutePath();
      final Path tomlFile = outputDirCreated.resolve(address + ".toml").toAbsolutePath();

      final String toml =
          new TomlStringBuilder("signing")
              .withQuotedString("type", "file-based-signer")
              .withQuotedString("key-file", keyFile.toString())
              .withQuotedString("password-file", passwordFile.toString())
              .build();
      Files.writeString(tomlFile, toml);
      Files.writeString(passwordFile, password);

      LOG.info("Generated EthSigner file based signer configuration files for address {}" + address);
    }

    return 0;
  }
}
