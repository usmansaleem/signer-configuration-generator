package web3signer.hashicorp.loader;

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
