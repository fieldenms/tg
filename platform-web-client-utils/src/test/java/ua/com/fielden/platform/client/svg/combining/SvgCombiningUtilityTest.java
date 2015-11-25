package ua.com.fielden.platform.client.svg.combining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class SvgCombiningUtilityTest {


    @Test
    public void output_file_sould_contain_each_src_file() throws IOException   {

        final String args[] = new String[2];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";

        final String outputFile = "src/test/resources/combiningSvgResult.html";

        final String contentFirs = Files.toString(new File(args[0]), Charsets.UTF_8);
        final String contentSecond = Files.toString(new File(args[1]), Charsets.UTF_8);

        final String contentOutputFile = Files.toString(new File(outputFile), Charsets.UTF_8);

        final SvgCombiningUtility svgCombiningUtility = new SvgCombiningUtility();

        svgCombiningUtility.combineSvgFilesContent(args, outputFile, 1000, "name");

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
            svgCombiningUtility.combineSvgFilesContent(args, outputFile, 1000, "name");
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Dest file does not exist!", ex.getMessage());
        }

    }

}
