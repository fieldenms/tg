package ua.com.fielden.platform.web.menu.iconset;

import java.io.IOException;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

public class SvgIconset {

    public static void main(final String[] args) throws IOException {
        final String srcFolder = "src/main/resources/images/svg";
        final String srcFolderDet = "src/main/resources/images/detailed/svg";
        final String iconsetId = "menu";
        final String iconsetIdDet = "menu-detailed";
        final int svgWidth = 1000;
        final String outputFile = "src/main/resources/images/menu.js";
        final String outputFileDet = "src/main/resources/images/detailed/menu-detailed.js";

        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth, srcFolder);
        final IronIconsetUtility iconsetUtilityDet = new IronIconsetUtility(iconsetIdDet, svgWidth, srcFolderDet);
        iconsetUtility.createSvgIconset(outputFile);
        iconsetUtilityDet.createSvgIconset(outputFileDet);

    }

}
