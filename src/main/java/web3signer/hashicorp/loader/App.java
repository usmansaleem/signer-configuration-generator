package web3signer.hashicorp.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

@Command(
    name = "web3signer-configuration-loader",
    subcommands = {HashicorpSubcommand.class, RawSubcommand.class},
    mixinStandardHelpOptions = true,
    version = "1.0",
    description =
        "Generate random BLS Keys and load them to Hashicorp and generates web3signer configuration files to access them.")
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
