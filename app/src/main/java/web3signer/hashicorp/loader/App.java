package web3signer.hashicorp.loader;

import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import tech.pegasys.teku.bls.BLSKeyPair;

@Command(
    name = "web3signer-hashicorp-loader",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description =
        "Generate random BLS Keys and load them to Hashicorp and generates web3signer configuration files to access them.")
public class App implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  @Option(
      names = {"--url"},
      description = "Hashicorp API URL. Default: ${DEFAULT-VALUE}")
  private URI hashicorpUrl = URI.create("http://localhost:8200/v1/secret");

  @Option(
      names = {"--token"},
      description = "Hashicorp token",
      required = true)
  private String token;

  @Option(
      names = "--output",
      description =
          "Output directory for web3signer configuration files. Default: ${DEFAULT-VALUE}")
  private Path outputDir = Path.of("./keys");

  @Option(
      names = "--count",
      converter = PositiveIntegerConverter.class,
      description = "Number of keys to generate and insert. Default: ${DEFAULT-VALUE}")
  private int count = 50;

  @Override
  public Integer call() {
    final HashicorpVaultClient hashicorpVaultClient = new HashicorpVaultClient(hashicorpUrl, token);
    if (!hashicorpVaultClient.isInitialized()) {
      return -1;
    }

    LOG.info("Generating {} BLS Keys", count);
    final Set<BLSKeyPair> blsKeyPairs = new BLSKeyGenerator().generate(count);

    LOG.info("Inserting into hashicorp...");
    final Set<String> publicKeys = hashicorpVaultClient.insertSecret(blsKeyPairs);

    LOG.info("Creating Web3Signer configuration files in {}", outputDir);

    if (publicKeys.isEmpty()) {
      LOG.warn("No keys to create in output directory");
    } else {
      final Web3SignerYamlConfiguration web3SignerYamlConfiguration =
          new Web3SignerYamlConfiguration(hashicorpUrl, outputDir, token);
      web3SignerYamlConfiguration.createHashicorpYamlConfigurationFiles(publicKeys);
    }

    return 0;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new App()).execute(args));
  }
}
