package ua.com.fielden.platform.web.menu.iconset;

import java.io.IOException;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

public class SvgButtonsIconset {
    public static void main(final String[] args) throws IOException {
        final String srcFolder = "src/main/resources/images/collapse-expand";
        final String iconsetId = "collapseExpand";
        final int svgWidth = 24;
        final String outputFile = "src/main/resources/images/collapseExpand.html";

        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
    }
}
