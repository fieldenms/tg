package ua.com.fielden.platform.client.svg.combining;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

public class SvgIconsetUtilityTest {

    @Test
    public void output_file_should_contain_each_src_file() throws IOException {
        final String srcFolder = "src/test/resources/icons";
        final Set<String> srcFiles = new HashSet<String>();
        srcFiles.add("src/test/resources/icons/icon.svg");
        srcFiles.add("src/test/resources/icons/icon.svg");
        final String outputFile = "src/test/resources/combiningSvgResult.html";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
        final String contentOutputFile = Files.asCharSource(new File(outputFile), Charsets.UTF_8).read();

        for (final String file : srcFiles) {
            final String contentFile = Files.asCharSource(new File(file), Charsets.UTF_8).read();
            assertTrue(contentOutputFile.contains(contentFile));
        }
    }

    @Test
    public void output_file_should_contain_correct_begin_and_end() throws IOException {
        final String srcFolder = "src/test/resources/icons";
        final String outputFile = "src/test/resources/combiningSvgResult.html";
        final String fileBegin = format(IronIconsetUtility.FILE_BEGIN_TEMPLATE, "testName", 1000);
        final String fileEnd = IronIconsetUtility.FILE_END_TEMPLATE;
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000, srcFolder);
        iconsetUtility.createSvgIconset(outputFile);
        final String contentOutputFile = Files.asCharSource(new File(outputFile), Charsets.UTF_8).read();
        assertTrue(contentOutputFile.startsWith(fileBegin) && contentOutputFile.endsWith(fileEnd));
    }

    @Test
    public void joined_files_string_should_contain_correct_number_of_svg_elements() throws IOException {
        final String srcFolder = "src/test/resources/icons";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000, srcFolder);
        assertEquals(3, StringUtils.countMatches(iconsetUtility.joinFilesContent(), "<svg"));
        assertEquals(3, StringUtils.countMatches(iconsetUtility.joinFilesContent(), "</svg>"));
    }

    @Test
    public void joined_files_string_should_contain_svg_id() throws IOException {
        final String srcFolder = "src/test/resources/icons";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000, srcFolder);
        assertEquals(2, (StringUtils.countMatches(iconsetUtility.joinFilesContent(), "id=\"svg2\"")));
    }

    @Test
    public void file_not_able_to_be_written_cannot_be_an_argument() throws IOException {
        final String srcFolder = "src/test/resources/icons";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000, srcFolder);
        final String outputFile = "src/test/resources/fileNotForWriting.html";
        final File readOnly = new File(outputFile);
        readOnly.setReadOnly();

        try {
            iconsetUtility.createSvgIconset(outputFile);
            fail();
        } catch (final IOException ex) {
        }
    }

    @Test
    public void src_files_should_be_valid() throws IOException {
        final String srcFolder = "src/test/resources/testIcons";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000, srcFolder);
        final String outputFile = "src/test/resources/combiningSvgResult.html";

        try {
            iconsetUtility.createSvgIconset(outputFile);
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Not valid file! Src file should not be empty.", ex.getMessage());
        }
    }

    @Test
    public void src_folder_should_be_not_empty() throws IOException {
        final String srcFolder = "src/test/resources/emptyFolder";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000, srcFolder);
        final String outputFile = "src/test/resources/combiningSvgResult.html";

        try {
            iconsetUtility.createSvgIconset(outputFile);
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Empty src directory!", ex.getMessage());
        }
    }

}
