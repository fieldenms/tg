package ua.com.fielden.platform.web.menu.iconset;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

import java.io.IOException;

/**
 * The icon-set creator for reference hierarchy
 *
 * @author TG Team
 *
 */
public class RichTextEditorIconsetCreator {

    public static void main(final String[] args) throws IOException {
        final String srcFolder = "src/main/resources/images/rich-text-editor";
        final String iconsetId = "tg-rich-text-editor";
        final int svgWidth = 24;
        final String outputFile = "src/main/resources/images/tg-rich-text-editor-icons.js";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
    }
}
