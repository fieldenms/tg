package ua.com.fielden.platform.client.svg.combining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import ua.com.fielden.platform.svg.combining.SvgCombiningUtility;

public class SvgCombiningUtilityTest {

    @Test
    public void output_file_should_contain_each_src_file() throws IOException {

        final String args[] = new String[2];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        final String outputFile = "src/test/resources/combiningSvgResult.html";

        final String contentFirs = Files.toString(new File(args[0]), Charsets.UTF_8);
        final String contentSecond = Files.toString(new File(args[1]), Charsets.UTF_8);
        final String contentOutputFile = Files.toString(new File(outputFile), Charsets.UTF_8);

        final SvgCombiningUtility svgCombiningUtility = new SvgCombiningUtility();

        svgCombiningUtility.combineSvgFilesContent(args, outputFile, "testName", "1000");

        assertTrue(contentOutputFile.contains(contentFirs));
        assertTrue(contentOutputFile.contains(contentSecond));
    }

    @Test
    public void file_wich_does_not_exist_can_not_be_an_argument() throws IOException {

        final String args[] = new String[2];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        final String outputFile = "src/test/resources/notExistedFile.html";
        final SvgCombiningUtility svgCombiningUtility = new SvgCombiningUtility();

        try {
            svgCombiningUtility.combineSvgFilesContent(args, outputFile, "testName", "1000");
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Src or dest file does not exist!", ex.getMessage());
        }
    }

    @Test
    public void wrong_size_can_not_be_an_argument() throws IOException {

        final String args[] = new String[2];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        final String outputFile = "src/test/resources/combiningSvgResult.html";
        final SvgCombiningUtility svgCombiningUtility = new SvgCombiningUtility();

        try {
            svgCombiningUtility.combineSvgFilesContent(args, outputFile, "testName", "a100");
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Type of size have to be integer!", ex.getMessage());
        }
    }

    @Test
    public void file_wich_is_not_able_to_be_writed_can_not_be_an_argument() throws IOException {

        final String args[] = new String[2];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        final String outputFile = "src/test/resources/fileNotForWriting.html";
        final SvgCombiningUtility svgCombiningUtility = new SvgCombiningUtility();

        try {
            svgCombiningUtility.combineSvgFilesContent(args, outputFile, "testName", "1000");
            fail();
        } catch (final IOException ex) {
            assertEquals("Something is wrong! Iron-iconset-svg was not created!", ex.getMessage());
        }
    }
}
