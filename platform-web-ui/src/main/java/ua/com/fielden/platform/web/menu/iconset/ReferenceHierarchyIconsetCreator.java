package ua.com.fielden.platform.web.menu.iconset;

import java.io.IOException;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

/**
 * The icon-set creator for reference hierarchy
 *
 * @author TG Team
 *
 */
public class ReferenceHierarchyIconsetCreator {

    public static void main(final String[] args) throws IOException {
        final String srcFolder = "src/main/resources/images/reference_hierarchy";
        final String iconsetId = "tg-reference-hierarchy";
        final int svgWidth = 24;
        final String outputFile = "src/main/resources/images/tg-reference-hierarchy.js";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
    }
}
