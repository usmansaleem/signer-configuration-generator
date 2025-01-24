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

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import tech.pegasys.teku.bls.BLSKeyPair;

@Command(name = "keystores")
public class KeystoresSubcommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(KeystoresSubcommand.class);

  @Option(
      names = "--output",
      description =
          "Output directory for web3signer configuration files and encrypted keystores. Default: ${DEFAULT-VALUE}")
  Path outputDir = Path.of("./keys");

  @Option(
      names = "--outputDirInConfig",
      description =
          "Output directory that should be reported in config files. This can be changed to reflect paths in docker environment. Default: ${DEFAULT-VALUE}")
  Path outputDirInConfig = Path.of("./keys");

  @Option(
      names = "--count",
      converter = PositiveIntegerConverter.class,
      description = "Number of keys to generate and insert. Default: ${DEFAULT-VALUE}")
  int count = 50;

  @Override
  public Integer call() {
    LOG.info("Generating {} BLS Keys", count);
    final Set<BLSKeyPair> blsKeyPairs = new BLSKeyGenerator().generate(count);

    LOG.info("Creating Web3Signer configuration files and keystores in {}", outputDir);
    new Web3SignerYamlConfiguration(outputDir)
        .createKeystoreConfigurationFiles(blsKeyPairs, outputDirInConfig);
    return 0;
  }
}
