package web3signer.hashicorp.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.pegasys.teku.bls.BLSKeyPair;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

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
