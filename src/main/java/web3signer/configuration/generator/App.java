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

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import web3signer.configuration.util.VersionProvider;

@Command(
    name = "signer-configuration-generator",
    subcommands = {HashicorpSubcommand.class, RawSubcommand.class},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description =
        "Generate random BLS Keys and web3signer configuration files (and load them in vault)")
public class App implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(App.class);
  @Spec CommandSpec spec;

  @Override
  public Integer call() {
    throw new ParameterException(spec.commandLine(), "Specify a subcommand");
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new App()).execute(args));
  }
}
