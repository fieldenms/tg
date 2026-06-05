package ua.com.fielden.platform.utils;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.types.Colour;

import java.awt.*;

import static org.apache.logging.log4j.LogManager.getLogger;

/// Utilities for manipulating colour, which relies on AWT.
///
public class ColourUtils {

    private final static Logger LOGGER = getLogger();

    private ColourUtils() {}

    /// The same as [#darkenHexColor(String, int)], but in application to [Colour].
    ///
    public static Colour darkenColor(final Colour colour, final int nTimes) {
        return new Colour(darkenColor_(colour.getColourValue(), nTimes));
    }

    /// Calculates a colour that is darkened `nTimes`, using `hexColour` as the basis.
    ///
    /// @param hexColour a hex colour, which should include `#`; for example, `#c2ab18`.
    /// @param nTimes    expresses the number of times the colour should be darkened.
    ///
    /// @return a hex colour, which includes `#` at the beginning of the value.
    ///
    public static String darkenHexColor(final String hexColour, final int nTimes) {
        return "#" + darkenColor_(hexColour, nTimes);
    }

    /// The same as [#brightenHexColor(String, int)], but in application to [Colour].
    ///
    public static Colour brightenColor(final Colour colour, final int nTimes) {
        return new Colour(brightenHexColor_(colour.getColourValue(), nTimes));
    }

    /// Calculates a colour that is brighten `nTimes`, using `hexColour` as the basis.
    ///
    /// @param hexColour a hex colour, which should include `#`; for example, `#c2ab18`.
    /// @param nTimes    expresses the number of times the colour should be brightened.
    ///
    /// @return a hex colour, which includes `#` at the beginning of the value.
    ///
    public static String brightenHexColor(final String hexColour, final int nTimes) {
        return "#" + brightenHexColor_(hexColour, nTimes);
    }


    /// Calculates a colour that is brightened `nTimes`, using `hexColour` as the basis.
    ///
    /// Brighter in this case means [Color#brighter()], and `nTimes` controls how many times [Color#brighter()] is called.
    /// This approach of brightening is not very precise, but it produces good results and is easy to use consistently.
    ///
    ///
    /// @param hexColour a hex colour, which should include `#`; for example, `#c2ab18`.
    /// @param nTimes    expresses the number of times the colour should be brightened;
    ///                  for example, to get a colour darkened twice, specify `nTimes = 2`.
    ///
    /// @return a lighter colour or the original colour in case of an exception;
    ///         the returned value does not include `#`.
    ///
    private static String brightenHexColor_(final String hexColour, final int nTimes) {
        try {
            // Convert hexColour to Color object.
            var color = Color.decode(hexColour);
            // Apply brighter() method `nTimes` to increase lightness.
            for (int i = 0; i < nTimes; i++) {
                color = color.brighter();
            }
            return "%02X%02X%02X".formatted(color.getRed(), color.getGreen(), color.getBlue());
        } catch (final Exception ex) {
            LOGGER.warn("Could not convert the colour.", ex);
            return hexColour;
        }

    }

    /// Calculates a colour that is darkened `nTimes`, using `hexColour` as the basis.
    ///
    /// Darker in this case means [Color#darker()], and `nTimes` controls how many times [Color#darker()] is called.
    /// This approach of darkening is not very precise, but it produces good results and is easy to use consistently.
    ///
    ///
    /// @param hexColour a hex colour, which should include `#`; for example, `#c2ab18`.
    /// @param nTimes    expresses the number of times the colour should be darkened;
    ///                  for example, to get a colour darkened twice, specify `nTimes = 2`.
    ///
    /// @return a darkened colour or the original colour in case of an exception;
    ///         the returned value does not include `#`.
    ///
    private static String darkenColor_(final String hexColour, final int nTimes) {
        try {
            // Convert hexColour to Color object.
            var color = Color.decode(hexColour);
            // Apply darker() method `nTimes` to increase darkness.
            for (int i = 0; i < nTimes; i++) {
                color = color.darker();
            }
            return "%02X%02X%02X".formatted(color.getRed(), color.getGreen(), color.getBlue());
        } catch (final Exception ex) {
            LOGGER.warn("Could not convert the colour.", ex);
            return hexColour;
        }
    }

}
