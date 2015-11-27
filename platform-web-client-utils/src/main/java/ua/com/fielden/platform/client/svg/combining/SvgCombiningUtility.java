package ua.com.fielden.platform.client.svg.combining;

import static java.nio.file.Files.readAllBytes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.google.common.base.Charsets;

public class SvgCombiningUtility {

    private final int size;
    private final String name;

    public SvgCombiningUtility(final int size, final String name) {
        this.name = name;
        this.size = size;
    }

    public void combineSvgFilesContent(final String[] srcFiles, final String outputFile) throws IOException {

        final Validator validator = new Validator();
        for (final String file : srcFiles) {
            validator.validate(file);
        }
        validator.validate(outputFile);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            write(outputStream, getAndJoinContentOfFiles(srcFiles) + "\n");
        } catch (final IOException e) {
            throw new IOException("Something is wrong! Iron-iconset-svg was not created!");
        }
    }

    private OutputStream write(final OutputStream outputStream, final String string) throws IOException {
        final String fileBegin = new FileBegin(name, size).getFileBegin();
        final String fileEnd = new FileEnd().getFileEnd();
        outputStream.write(fileBegin.getBytes(Charsets.UTF_8));
        outputStream.write((string).getBytes(Charsets.UTF_8));
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
