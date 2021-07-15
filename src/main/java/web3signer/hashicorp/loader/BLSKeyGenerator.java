package web3signer.hashicorp.loader;

import java.security.SecureRandom;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import tech.pegasys.teku.bls.BLSKeyPair;

public class BLSKeyGenerator {
  private final SecureRandom secureRandom = new SecureRandom();

  public Set<BLSKeyPair> generate(final int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> BLSKeyPair.random(secureRandom))
        .collect(Collectors.toSet());
  }
}
