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
        final String outputFile = "src/main/resources/images/menu.html";
        final String outputFileDet = "src/main/resources/images/detailed/menu-detailed.html";

        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth);
        final IronIconsetUtility iconsetUtilityDet = new IronIconsetUtility(iconsetIdDet, svgWidth);
        iconsetUtility.createSvgIconset(srcFolder, outputFile);
        iconsetUtilityDet.createSvgIconset(srcFolderDet, outputFileDet);


    }

}
