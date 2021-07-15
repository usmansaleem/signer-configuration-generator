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
import picocli.CommandLine.TypeConversionException;

public class PositiveIntegerConverter implements CommandLine.ITypeConverter<Integer> {
  @Override
  public Integer convert(final String value) throws TypeConversionException {
    try {
      final int count = Integer.parseInt(value);
      if (count <= 0) {
        throw new TypeConversionException(
            String.format("Expecting positive number but was '" + value + "'", count));
      }
      return count;
    } catch (final NumberFormatException e) {
      throw new TypeConversionException(
          "Invalid format: expecting numeric value but was '" + value + "'");
    }
  }
}
