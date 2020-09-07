package ua.com.fielden.platform.svg.combining;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

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
    public static final String FILE_BEGIN_TEMPLATE = "import '/resources/polymer/@polymer/iron-icon/iron-icon.js';%n" +
                                                     "import '/resources/polymer/@polymer/iron-iconset-svg/iron-iconset-svg.js';%n" +
                                                     "import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';%n" +
                                                     "const template = html`<iron-iconset-svg name='%s' size='%d'> %n <svg> %n <defs> %n";
    public static final String FILE_END_TEMPLATE = "</defs> \n </svg> \n </iron-iconset-svg>`;\n" +
                                                   "document.head.appendChild(template.content);";
    private final String fileBegin;
    private final String fileEnd;
    private final String srcFolder;

    public IronIconsetUtility(final String iconsetId, final int svgWidth, final String srcFolder) {
        this.fileBegin = format(FILE_BEGIN_TEMPLATE, iconsetId, svgWidth);
        this.fileEnd = FILE_END_TEMPLATE;
        this.srcFolder = srcFolder;
    }

    public void createSvgIconset(final String outputFile) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(joinFilesContent().getBytes(Charsets.UTF_8));
        }
    }

    public String joinFilesContent() throws IOException {
        final Set<String> files = getFilesFromFolder(srcFolder);
        for (final String file : files) {
            final File fileToValidate = new File(file);
            if (fileToValidate.length() == 0) {
                throw new IllegalArgumentException("Not valid file! Src file should not be empty.");
            }
        }
        final StringBuilder joinedFilesConent = new StringBuilder().append(fileBegin);
        for (final String file : files) {
            final String fileContent = new String(readAllBytes(Paths.get(file)));
            joinedFilesConent.append(fileContent);
        }
        return joinedFilesConent.append(fileEnd).toString();
    }

    private Set<String> getFilesFromFolder(final String folder) throws IOException {
        final Set<String> srcFiles = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folder), "*.svg")) {
            for (final Path filePath : stream) {
                srcFiles.add(filePath.toString());
            }
        } catch (final DirectoryIteratorException ex) {
            throw ex.getCause();
        }
        if (!srcFiles.iterator().hasNext()) {
            throw new IllegalArgumentException("Empty src directory!");
        }
        return srcFiles;
    }
}
