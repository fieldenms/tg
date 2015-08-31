package ua.com.fielden.platform.types;

public class Colour {
    private final String hashlessUppercasedColourValue;

    public static Colour BLACK = new Colour("000000");
    public static Colour RED = new Colour("FF0000");
    public static Colour WHITE = new Colour("FFFFFF");
    
    public Colour(final String colorValue) {
        validateColourValue(colorValue);
        this.hashlessUppercasedColourValue = colorValue.toUpperCase();
    }

    private void validateColourValue(final String colourValue) {
        if (!colourValue.matches("\\p{XDigit}{3}|\\p{XDigit}{6}")) {
            throw new IllegalArgumentException(
                    "Colour value string must be composed of either 3 or 6 hexadecimal chars");
        }
    }

    public String getColourValue() {
        return "#" + hashlessUppercasedColourValue;
    }

    @Override
    public String toString() {
        return hashlessUppercasedColourValue;
    }
}