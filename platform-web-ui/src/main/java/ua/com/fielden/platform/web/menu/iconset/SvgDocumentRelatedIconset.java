package ua.com.fielden.platform.web.menu.iconset;

import java.io.IOException;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

public class SvgDocumentRelatedIconset {
    public static void main(final String[] args) throws IOException {
        final String srcFolder = "src/main/resources/images/document-related";
        final String iconsetId = "tg-document-related-icons";
        final int svgWidth = 24;
        final String outputFile = "src/main/resources/images/tg-document-related-icons.js";

        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
    }
}
