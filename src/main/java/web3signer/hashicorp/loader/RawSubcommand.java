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
package web3signer.hashicorp.loader;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.pegasys.teku.bls.BLSKeyPair;

@CommandLine.Command(name = "raw")
public class RawSubcommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(HashicorpSubcommand.class);

  @CommandLine.Option(
      names = "--output",
      description =
          "Output directory for web3signer configuration files. Default: ${DEFAULT-VALUE}")
  Path outputDir = Path.of("./keys");

  @CommandLine.Option(
      names = "--count",
      converter = PositiveIntegerConverter.class,
      description = "Number of keys to generate and insert. Default: ${DEFAULT-VALUE}")
  int count = 50;

  @Override
  public Integer call() {
    LOG.info("Generating {} BLS Keys", count);
    final Set<BLSKeyPair> blsKeyPairs = new BLSKeyGenerator().generate(count);

    LOG.info("Creating Web3Signer configuration files in {}", outputDir);
    new Web3SignerYamlConfiguration(outputDir).createRawYamlConfigurationFiles(blsKeyPairs);
    return 0;
  }
}
