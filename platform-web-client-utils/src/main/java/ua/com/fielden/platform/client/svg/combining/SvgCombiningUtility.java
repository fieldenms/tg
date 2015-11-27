package ua.com.fielden.platform.client.svg.combining;

import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readAllBytes;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.google.common.base.Charsets;

public class SvgCombiningUtility {


    public void combineSvgFilesContent(final String[] srcFiles, final String outputFile, final int size, final String name) throws IOException {

        final String fileBegin = new FileBegin(name, size).getFileBegin();

        final String fileEnd = new FileEnd().getFileEnd();

        for (final String file : srcFiles) {
            if (isEmpty(file) || notExists(new File(file).toPath())) {
                throw new IllegalArgumentException("One or more src file does not exist!");
            }
        }

        if (isEmpty(outputFile) || notExists(new File(outputFile).toPath())) {
            throw new IllegalArgumentException("Dest file does not exist!");
        }

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(fileBegin.getBytes(Charsets.UTF_8));
            outputStream.write((getAndJoinContentOfFiles(srcFiles) + "\n").getBytes(Charsets.UTF_8));
            outputStream.write(fileEnd.getBytes(Charsets.UTF_8));
        }
    }

    private String getAndJoinContentOfFiles(final String[] srcFile) throws IOException {
        return joinFilesContent(0, srcFile, "");
    }

    private String joinFilesContent(final int fileToCopy, final String[] files, final String joinedFilesConent) throws IOException{
        if (fileToCopy == files.length) {
            return joinedFilesConent;
        } else {
            final String fileContent = new String(readAllBytes(Paths.get(files[fileToCopy])));
            return joinFilesContent(fileToCopy + 1, files, joinedFilesConent + "\n" + fileContent);
        }
        }
    }
