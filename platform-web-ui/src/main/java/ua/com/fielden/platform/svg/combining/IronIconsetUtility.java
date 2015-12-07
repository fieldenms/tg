package ua.com.fielden.platform.svg.combining;

import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readAllBytes;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Charsets;

public class IronIconsetUtility {

    private final String fileBegin;
    private final String fileEnd;
    private final String iconsetId;
    private final String svgWidth;

    public IronIconsetUtility(final String iconsetId, final String svgWidth) {
        this.iconsetId = iconsetId;
        this.svgWidth = svgWidth;
        this.fileBegin = "<link rel=\"import\" href=\"/resources/polymer/iron-icon/iron-icon.html\">" +
                "\n <link rel=\"import\" href=\"/resources/polymer/iron-iconset-svg/iron-iconset-svg.html\">" +
                "\n <iron-iconset-svg name=\"" + this.iconsetId + "\" size=\"" + this.svgWidth + "\">" + "\n <svg>" + "\n <defs>; \n";
        this.fileEnd = "</defs>" + "\n </svg>" + "\n </iron-iconset-svg>";
    }

    public void createSvgIconset(final String srcFolder, final String outputFile) throws IOException {
        final Set<String> srcFiles = getFilesFromFolder(srcFolder);
        for (final String file : srcFiles) {
            validateSrcFile(file);
        }
        validateSvgWidth(svgWidth);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            writeAllFilesContent(outputStream, joinFilesContent(srcFiles, "") + "\n");
            System.out.println("Iron-iconset-svg creation is complete!");
        } catch (final IOException e) {
            throw new IOException();
        }
    }

    private OutputStream writeAllFilesContent(final OutputStream outputStream, final String filesContent) throws IOException {
        outputStream.write(fileBegin.getBytes(Charsets.UTF_8));
        outputStream.write((filesContent).getBytes(Charsets.UTF_8));
        outputStream.write(fileEnd.getBytes(Charsets.UTF_8));
        return outputStream;

    }

    private String joinFilesContent(final Set<String> files, String joinedFilesConent) throws IOException {
        for (final String file : files) {
            final String fileContent = new String(readAllBytes(Paths.get(file)));
            joinedFilesConent = joinedFilesConent + (fileContent);
        }
        return joinedFilesConent;
    }

    private void validateSrcFile(final String file) {
        if (isEmpty(file) || notExists(new File(file).toPath())) {
            throw new IllegalArgumentException();
        }
    }

    private void validateSvgWidth(final String string) {
        try {
            Integer.valueOf(string);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }

    private Set<String> getFilesFromFolder(final String folder) throws IOException {
        final Set<String> srcFiles = new HashSet();
        Files.walk(Paths.get(folder)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                srcFiles.add(filePath.toString());
            }
        });
        return srcFiles;
    }
}
