package ua.com.fielden.platform.svg.combining;

import static java.nio.file.Files.readAllBytes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.google.common.base.Charsets;

public class IronIconsetUtility {

    private final String fileBegin;
    private final String fileEnd;

    public IronIconsetUtility(final String iconsetId, final String svgWidth) {
        this.fileBegin = "<link rel=\"import\" href=\"/resources/polymer/iron-icon/iron-icon.html\">" +
                "\n <link rel=\"import\" href=\"/resources/polymer/iron-iconset-svg/iron-iconset-svg.html\">" +
                "\n <iron-iconset-svg name=\"" + iconsetId + "\" size=\"" + svgWidth + "\">" + "\n <svg>" + "\n <defs>; \n";
        this.fileEnd = "</defs>" + "\n </svg>" + "\n </iron-iconset-svg>";

    }

    public void createSvgIconset(final String[] srcFiles, final String outputFile, final String iconsetId, final String svgWidth) throws IOException {
        final Validator validator = new Validator();
        for (final String file : srcFiles) {
            validator.validate(file);
        }
        validator.validate(outputFile);
        validator.validateInt(svgWidth);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
           writeAllFilesContent(outputStream, getAndJoinContentOfFiles(srcFiles) + "\n");
            System.out.println("Iron-iconset-svg creation is complete!");
        } catch (final IOException e) {
            throw new IOException("Something is wrong! Iron-iconset-svg was not created!");
        }
    }

    private OutputStream writeAllFilesContent(final OutputStream outputStream, final String filesContent) throws IOException {
        outputStream.write(fileBegin.getBytes(Charsets.UTF_8));
        outputStream.write((filesContent).getBytes(Charsets.UTF_8));
        outputStream.write(fileEnd.getBytes(Charsets.UTF_8));
        return outputStream;

    }

    private String getAndJoinContentOfFiles(final String[] srcFile) throws IOException {
        return joinFilesContent(0, srcFile, "");
    }

    private String joinFilesContent(final int fileToCopy, final String[] files, final String joinedFilesConent) throws IOException {
        if (fileToCopy == files.length) {
            return joinedFilesConent;
        } else {
            final String fileContent = new String(readAllBytes(Paths.get(files[fileToCopy])));
            return joinFilesContent(fileToCopy + 1, files, joinedFilesConent + "\n" + fileContent);
        }
    }
}
