package ua.com.fielden.platform.web.menu.iconset;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

import java.io.IOException;

/**
 * The icon-set creator for rich text editor
 *
 * @author TG Team
 *
 */
public class EditorIconsetCreator {

    public static void main(final String[] args) throws IOException {
        final String srcFolder = "src/main/resources/images/editor-related";
        final String iconsetId = "editor-icons";
        final int svgWidth = 24;
        final String outputFile = "src/main/resources/images/editor-icons.js";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
    }
}
