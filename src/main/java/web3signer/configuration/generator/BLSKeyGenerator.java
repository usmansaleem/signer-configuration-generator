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
