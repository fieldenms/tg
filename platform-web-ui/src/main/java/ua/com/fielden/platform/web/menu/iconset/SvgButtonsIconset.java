package ua.com.fielden.platform.web.menu.iconset;

import static java.lang.String.format;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

public class SvgButtonsIconset {
    public static void main(final String[] args) throws IOException {
        final String numberSvgTemplate = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
                "<svg id=\"number@\" xmlns=\"http://www.w3.org/2000/svg\" height=\"24\" width=\"24\" version=\"1.1\">\n" + 
                "    <text x=\"7\" y=\"19\" style=\"font-size:13pt;\">@</text>\n" + 
                "</svg>";
        for (int index = 1; index <= 9; index++) { 
            try (OutputStream outputStream = new FileOutputStream(format("src/main/resources/images/collapse-expand/number%s.svg", index))) {
                outputStream.write(numberSvgTemplate.replace('@', (index + "").charAt(0)).getBytes(Charsets.UTF_8));
            }
        }
        
        final String srcFolder = "src/main/resources/images/collapse-expand";
        final String iconsetId = "tg-icons";
        final int svgWidth = 24;
        final String outputFile = "src/main/resources/images/tg-icons.js";

        final IronIconsetUtility iconsetUtility = new IronIconsetUtility(iconsetId, svgWidth, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
    }
}
