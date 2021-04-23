package web3signer.hashicorp.loader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class Web3SignerYamlConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(Web3SignerYamlConfiguration.class);
  private final URI hashicorpApiEndpoint;
  private final Path outputDir;
  private final String token;
  private final DumperOptions options = new DumperOptions();

  public Web3SignerYamlConfiguration(
      final URI hashicorpApiEndpoint, final Path outputDir, final String token) {
    this.hashicorpApiEndpoint = hashicorpApiEndpoint;
    this.outputDir = outputDir;
    this.token = token;
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    LOG.debug("Creating output directory: {}", outputDir);
    try {
      Files.createDirectories(outputDir);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void createHashicorpYamlConfigurationFiles(final Set<String> publicKeys) {
    publicKeys.forEach(
        publicKey -> {
          final URI secretsEndpoint =
              URI.create(hashicorpApiEndpoint.toString() + "/data/" + publicKey).normalize();
          final String content = getHashicorpYamlConfiguration(secretsEndpoint);
          final Path outputFile = outputDir.resolve(publicKey + ".yaml");
          try {
            Files.writeString(outputFile, content);
          } catch (IOException e) {
            LOG.error("Error creating configuration file {}: {}", outputFile, e.getMessage());
          }
        });
  }

  private String getHashicorpYamlConfiguration(final URI uri) {
    // create configuration file
    final Map<String, Object> map = new HashMap<>();
    map.put("type", "hashicorp");
    map.put("keyPath", uri.getPath());
    map.put("keyName", "value");
    map.put("tlsEnabled", "https".equalsIgnoreCase(uri.getScheme()) ? "true" : "false");
    map.put("serverHost", uri.getHost());
    map.put("serverPort", uri.getPort());
    map.put("token", token);

    return new Yaml(options).dump(map);
  }
}
