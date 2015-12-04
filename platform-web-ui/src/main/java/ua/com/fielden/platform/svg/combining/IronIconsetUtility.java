package ua.com.fielden.platform.svg.combining;

import static java.nio.file.Files.readAllBytes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.google.common.base.Charsets;

public class IronIconsetUtility {

    public static void createSvgIconset(final String[] srcFiles, final String outputFile, final String iconsetId, final String svgWidth) throws IOException {
        final Validator validator = new Validator();
        for (final String file : srcFiles) {
            validator.validate(file);
        }
        validator.validate(outputFile);
        validator.validateInt(svgWidth);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
           writeAllFilesContent(outputStream, getAndJoinContentOfFiles(srcFiles) + "\n", iconsetId, svgWidth);
            System.out.println("Iron-iconset-svg creation is complete!");
        } catch (final IOException e) {
            throw new IOException("Something is wrong! Iron-iconset-svg was not created!");
        }
    }

    private static OutputStream writeAllFilesContent(final OutputStream outputStream, final String filesContent, final String iconsetId, final String svgWidth) throws IOException {
        final String fileBegin = new FileBegin(iconsetId, svgWidth).getFileBegin();
        final String fileEnd = new FileEnd().getFileEnd();
        outputStream.write(fileBegin.getBytes(Charsets.UTF_8));
        outputStream.write((filesContent).getBytes(Charsets.UTF_8));
        outputStream.write(fileEnd.getBytes(Charsets.UTF_8));
        return outputStream;

    }

    private static String getAndJoinContentOfFiles(final String[] srcFile) throws IOException {
        return joinFilesContent(0, srcFile, "");
    }

    private static String joinFilesContent(final int fileToCopy, final String[] files, final String joinedFilesConent) throws IOException {
        if (fileToCopy == files.length) {
            return joinedFilesConent;
        } else {
            final String fileContent = new String(readAllBytes(Paths.get(files[fileToCopy])));
            return joinFilesContent(fileToCopy + 1, files, joinedFilesConent + "\n" + fileContent);
        }
    }
}
