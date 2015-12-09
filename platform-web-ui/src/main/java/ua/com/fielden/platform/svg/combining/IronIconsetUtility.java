package ua.com.fielden.platform.svg.combining;

import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readAllBytes;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Charsets;

public class IronIconsetUtility {

    private final String fileBegin;
    private final String fileEnd;

    public IronIconsetUtility(final String iconsetId, final int svgWidth) {
        this.fileBegin = String.format("<link rel=\"import\" href=\"/resources/polymer/iron-icon/iron-icon.html\"> \n <link rel=\"import\" href=\"/resources/polymer/iron-iconset-svg/iron-iconset-svg.html\"> \n <iron-iconset-svg name=\"%s\" size=\"%d\"> \n <svg> \n <defs>; \n", iconsetId, svgWidth);
        this.fileEnd = "</defs> \n </svg> \n </iron-iconset-svg>";
    }

    public void createSvgIconset(final String srcFolder, final String outputFile) throws IOException {
        final Set<String> srcFiles = getFilesFromFolder(srcFolder);
        for (final String file : srcFiles) {
            validateSrcFile(file);
        }
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            writeAllFilesContent(outputStream, joinFilesContent(srcFiles) + "\n");
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

    private String joinFilesContent(final Set<String> files) throws IOException {
        final StringBuilder joinedFilesConent = new StringBuilder();
        for (final String file : files) {
            final String fileContent = new String(readAllBytes(Paths.get(file)));
            joinedFilesConent.append(fileContent);
        }
        return joinedFilesConent.toString();
    }

    private void validateSrcFile(final String file) {
        if (isEmpty(file) || notExists(new File(file).toPath())) {
            throw new IllegalArgumentException();
        }
    }

    private Set<String> getFilesFromFolder(final String folder) throws IOException {
        final Set<String> srcFiles = new HashSet<String>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folder), "*.svg")) {
            for (final Path filePath: stream) {
                srcFiles.add(filePath.toString());
            }
        } catch (final DirectoryIteratorException ex) {
            throw ex.getCause();
        }
        return srcFiles;
    }
}
