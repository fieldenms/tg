package ua.com.fielden.platform.web.menu.iconset;

import java.io.IOException;

import ua.com.fielden.platform.svg.combining.SvgIconsetUtility;

public class SvgIconset {
    public static void createIconsetSvg() throws IOException{
        final String[] args = new String[10];
        final String iconsetId = "menu";
        final String svgWidth = "1000";
        final String outputFile = "src/main/resources/images/menu.html";
        args[0] = "src/main/resources/images/accidents.svg";
        args[1] = "src/main/resources/images/divisionalDailyManagment.svg";
        args[2] = "src/main/resources/images/fleet.svg";
        args[3] = "src/main/resources/images/fuel.svg";
        args[4] = "src/main/resources/images/importUtilities.svg";
        args[5] = "src/main/resources/images/maintanance.svg";
        args[6] = "src/main/resources/images/onlineReports.svg";
        args[7] = "src/main/resources/images/organisational.svg";
        args[8] = "src/main/resources/images/preventiveMaintenence.svg";
        args[9] = "src/main/resources/images/user.svg";
        SvgIconsetUtility.createSvgIconset(args, outputFile, iconsetId, svgWidth);
    }

    public static void createDetailIconsetSvg() throws IOException {
        final String[] args = new String[10];
        final String iconsetId = "menu-detailed";
        final String svgWidth = "1000";
        final String outputFile = "src/main/resources/images/detailed/menu-detailed.html";
        args[0] = "src/main/resources/images/detailed/accidents.svg";
        args[1] = "src/main/resources/images/detailed/divisionalDailyManagment.svg";
        args[2] = "src/main/resources/images/detailed/fleet.svg";
        args[3] = "src/main/resources/images/detailed/fuel.svg";
        args[4] = "src/main/resources/images/detailed/importUtilities.svg";
        args[5] = "src/main/resources/images/detailed/maintanance.svg";
        args[6] = "src/main/resources/images/detailed/onlineReports.svg";
        args[7] = "src/main/resources/images/detailed/organisational.svg";
        args[8] = "src/main/resources/images/detailed/preventiveMaintenence.svg";
        args[9] = "src/main/resources/images/detailed/user.svg";
        SvgIconsetUtility.createSvgIconset(args, outputFile, iconsetId, svgWidth);
    }

    public static void main(final String[] args) throws IOException {
        createIconsetSvg();
        createDetailIconsetSvg();
    }

}
