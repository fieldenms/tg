package ua.com.fielden.platform.client.svg.combining;

import static java.nio.file.Files.readAllBytes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.google.common.base.Charsets;

public class SvgCombiningUtility {

    public static void main(final String[] args) throws IOException {
        final String[] srcFiles = new String[args.length - 3];
        final String name = args[0];
        final String size = args[1];
        final String outputFile = args[2];

        for (int i = 0; i < args.length-3; i++) {
            srcFiles[i] = args[i+3];
        }
        final SvgCombiningUtility combiningUtility = new SvgCombiningUtility();
        combiningUtility.combineSvgFilesContent(srcFiles, outputFile, name, size);
    }

    public void combineSvgFilesContent(final String[] srcFiles, final String outputFile, final String name, final String size) throws IOException {

        final Validator validator = new Validator();
        for (final String file : srcFiles) {
            validator.validate(file);
        }
        validator.validate(outputFile);
        validator.validateInt(size);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            write(outputStream, getAndJoinContentOfFiles(srcFiles) + "\n", name, size);
            System.out.println("Iron-iconset-svg creation is complete!");
        } catch (final IOException e) {
            throw new IOException("Something is wrong! Iron-iconset-svg was not created!");
        }
    }

    private OutputStream write(final OutputStream outputStream, final String string, final String name, final String size) throws IOException {
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
