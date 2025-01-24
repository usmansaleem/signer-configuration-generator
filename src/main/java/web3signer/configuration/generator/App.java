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

import picocli.CommandLine;
import picocli.CommandLine.Command;
import web3signer.configuration.util.VersionProvider;

@Command(
    name = "signer-configuration-generator",
    subcommands = {KeystoresSubcommand.class, HashicorpSubcommand.class, RawSubcommand.class},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description =
        "Generate random BLS Keys and web3signer configuration files (and load them in vault)")
public class App {
  public static void main(String[] args) {
    // bootstrap the application
    System.exit(new CommandLine(new App()).execute(args));
  }
}
