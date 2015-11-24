package ua.com.fielden.platform.client.svg.combining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Charsets;


public class SvgCombiningUtility {

    public SvgCombiningUtility() {
    }

    private static OutputStream outputStream;

    public static String getContentOfSvgFiles(final String srcFile) throws IOException{
        final String content = new String(Files.readAllBytes(Paths.get(srcFile)));
        return content;
    }

    public static void combineSvgFilesContent(final String[] srcAndOutputFiles, final int size, final String name ) throws IOException {

        final String fileBegin = "<link rel=\"import\" href=\"/resources/polymer/iron-icon/iron-icon.html\">" +
                "\n <link rel=\"import\" href=\"/resources/polymer/iron-iconset-svg/iron-iconset-svg.html\">" +
                "\n <iron-iconset-svg name=\"" + name + "\" size=\"" + size + "\">" + "\n <svg>" + "\n <defs>; \n";

        final String fileEnd = "</defs>" + "\n </svg>" + "\n </iron-iconset-svg>";

        final String outputFile = srcAndOutputFiles[srcAndOutputFiles.length - 1];

        for (int i = 0; i < srcAndOutputFiles.length - 1; i++) {
            if (StringUtils.isEmpty(srcAndOutputFiles[i]) || Files.notExists(new File(srcAndOutputFiles[i]).toPath())) {
                throw new IllegalArgumentException("One or more src file does not exist!");
            }
        }

        if (StringUtils.isEmpty(outputFile) || Files.notExists(new File(outputFile).toPath())) {
            throw new IllegalArgumentException("Dest file does not exist!");
        }

        outputStream = new FileOutputStream(outputFile);
        outputStream.write(fileBegin.getBytes(Charsets.UTF_8));
        for (int i = 0; i < srcAndOutputFiles.length - 1; i++) {
            outputStream.write((getContentOfSvgFiles(srcAndOutputFiles[i])+"\n").getBytes(Charsets.UTF_8));
        }
        outputStream.write(fileEnd.getBytes(Charsets.UTF_8));
        try {
            outputStream.close();
        } catch (final Exception e) {
        }

    }

}
