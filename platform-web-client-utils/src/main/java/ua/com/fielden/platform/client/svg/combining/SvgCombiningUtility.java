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

    private  OutputStream outputStream;

    private  String getContentOfSvgFiles(final String srcFile) throws IOException {
        final String content = new String(readAllBytes(Paths.get(srcFile)));
        return content;
    }

    public  void combineSvgFilesContent(final String[] srcFiles, final String outputFile,  final int size, final String name) throws IOException {

        final String fileBegin = new FileBegin(name, size).getFileBegin();

        final String fileEnd = new FileEnd().getFileEnd();

        for (int i = 0; i < srcFiles.length; i++) {
            if (isEmpty(srcFiles[i]) || notExists(new File(srcFiles[i]).toPath())) {
                throw new IllegalArgumentException("One or more src file does not exist!");
            }
        }

        if (isEmpty(outputFile) || notExists(new File(outputFile).toPath())) {
            throw new IllegalArgumentException("Dest file does not exist!");
        }
        outputStream = new FileOutputStream(outputFile);

        try {
            outputStream.write(fileBegin.getBytes(Charsets.UTF_8));
            for (int i = 0; i < srcFiles.length; i++) {
                outputStream.write((getContentOfSvgFiles(srcFiles[i]) + "\n").getBytes(Charsets.UTF_8));
            }
            outputStream.write(fileEnd.getBytes(Charsets.UTF_8));
        } finally {
            outputStream.close();
        }
    }
}
