package ua.com.fielden.platform.swing.utils;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.commons.lang.StringUtils;

/**
 * Provides convenient static utility methods for performing Java2D related tasks.
 *
 * @author TG Team
 *
 */
public class Utils2D {

    private Utils2D() {
    }

    /**
     * Abbreviates the text to fit into the specified width (the number of pixels) by substituting a non-fitting portion with triple dots.
     *
     * @param g2
     * @param text
     * @param fitToWidth
     * @return
     */
    public static String abbreviate(final Graphics2D g2, final String text, final int fitToWidth) {
	// define how many characters in the caption can be drawn
	final FontMetrics fm = g2.getFontMetrics();
	Rectangle2D textBounds = fm.getStringBounds(text, g2);
	int count = text.length();
	while ((textBounds.getWidth() > fitToWidth) && (count > 4)) {
	    textBounds = fm.getStringBounds(text.substring(0, count--), g2);
	}
	return count == text.length() ? text : StringUtils.abbreviate(text, count);
    }

    /**
     * Returns colour between <code>c1</code> and <code>c2</code> depending on the value of <code>ratio</code>, so that:
     * <ul>
     * <li>If <code>ratio</code> is equal to 0, <code>c1</code> is returned.</li>
     * <li>If <code>ratio</code> is equal to 1, <code>c2</code> is returned.</li>
     * </ul>
     *
     * @param c1
     * @param c2
     * @param ratio
     * @return
     */
    public static Color blend(final Color c1, final Color c2, final double ratio) {
	if (ratio < 0 || ratio > 1) {
	    throw new IllegalArgumentException("Blending ratio should be in a range from 0 to 1.");
	}
	final float[] c1Components = c1.getColorComponents(new float[3]);
	final float[] c2Components = c2.getColorComponents(new float[3]);

	return new Color((float) ((1 - ratio) * c1Components[0] + ratio * c2Components[0]),//
		(float) ((1 - ratio) * c1Components[1] + ratio * c2Components[1]),//
		(float) ((1 - ratio) * c1Components[2] + ratio * c2Components[2]));
    }

}
