package ua.com.fielden.platform.client.svg.combining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;


public class SvgCombiningUtility {

    public SvgCombiningUtility() {
    }

    private static OutputStream outputStream;

    private final static String fileBegin = "<link rel=\"import\" href=\"/resources/polymer/iron-icon/iron-icon.html\">" +
            "\n <link rel=\"import\" href=\"/resources/polymer/iron-iconset-svg/iron-iconset-svg.html\">" +
            "\n <iron-iconset-svg name=\"icon' size='1000\">" + "\n <svg>" + "\n <defs>; \n";

    private final static String fileEnd = "</defs>" + "\n </svg>" + "\n </iron-iconset-svg>";

    public static void svgCombining(final String[] srcFiles) throws IOException {

        final String destFile = srcFiles[srcFiles.length - 1];

        for (int i = 0; i < srcFiles.length - 1; i++) {
            if (StringUtils.isEmpty(srcFiles[i]) || Files.notExists(new File(srcFiles[i]).toPath())) {
                throw new IllegalArgumentException("One or more src file does not exist!");
            }
        }

        if (StringUtils.isEmpty(destFile) || Files.notExists(new File(destFile).toPath())) {
            throw new IllegalArgumentException("Dest file does not exist!");
        }

        outputStream = new FileOutputStream(destFile);
        outputStream.write(fileBegin.getBytes());
        for (int i = 0; i < srcFiles.length-1; i++) {
            final String content = new String(Files.readAllBytes(Paths.get(srcFiles[i]))) + "\n";
            outputStream.write(content.getBytes());
        }
        outputStream.write(fileEnd.getBytes());
        outputStream.close();

    }

    public static void main(final String[] args) throws IOException, URISyntaxException {

    }
}
