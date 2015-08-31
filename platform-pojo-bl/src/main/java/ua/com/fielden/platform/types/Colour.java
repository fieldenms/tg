package ua.com.fielden.platform.types;

public class Colour {
	private final String colourValue;

	public Colour(final String colorValue) {
		this.colourValue = checkColourValue(colorValue).toUpperCase();
	}

	private String checkColourValue(final String colourValue) {
		if (colourValue.length() == 3 || colourValue.length() == 6) {
			if (!colourValue.matches("\\p{XDigit}*")) {
				throw new IllegalArgumentException(
						"Colour value string must use only [0-9], [A-F]!");
			}
			return colourValue;
		} else {
			throw new IllegalArgumentException(
					"Colour value string length must be 6 or 3 only!");
		}
	}

	public String getColourValue() {
		return colourValue;
	}

}
