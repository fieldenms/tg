package ua.com.fielden.platform.swing.ei.editors;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A helper class for determining entity property editor's label and tooltip based on its type.
 * 
 * @author TG Team
 * 
 */
public class LabelAndTooltipExtractor {
    private LabelAndTooltipExtractor() {
    }

    public static String createTooltip(final String tooltip) {
	return StringUtils.isEmpty(tooltip) ? "" : "<html>filter by "
		+ (Character.isLetter(tooltip.charAt(1)) && Character.isUpperCase(tooltip.charAt(1)) ? tooltip : Character.toLowerCase(tooltip.charAt(0)) + tooltip.substring(1))
		+ "</html>";
    }

    public static String createCaption(final String caption) {
	return StringUtils.isEmpty(caption) ? "" : "filter by "
		+ (Character.isLetter(caption.charAt(1)) && Character.isUpperCase(caption.charAt(1)) ? caption : Character.toLowerCase(caption.charAt(0)) + caption.substring(1))
		+ "...";
    }

    public static Pair<String, String> extract(final String propertyName, final Class<?> entityType) {
	return TitlesDescsGetter.getTitleAndDesc(propertyName, entityType);
    }
}
