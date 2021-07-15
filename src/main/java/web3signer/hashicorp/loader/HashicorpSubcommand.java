package web3signer.hashicorp.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import tech.pegasys.teku.bls.BLSKeyPair;

import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "hashicorp")
public class HashicorpSubcommand implements Callable<Integer> {
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
    @CommandLine.Option(
            names = {"--url"},
            description = "Hashicorp API URL. Default: ${DEFAULT-VALUE}")
    private URI hashicorpUrl = URI.create("http://localhost:8200/v1/secret");

    @CommandLine.Option(
            names = {"--token"},
            description = "Hashicorp token",
            required = true)
    private String token;

    @Override
    public Integer call() throws Exception {
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
            new Web3SignerYamlConfiguration(outputDir).createHashicorpYamlConfigurationFiles(publicKeys, hashicorpUrl, token);
        }

        return 0;
    }
}
