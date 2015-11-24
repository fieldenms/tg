package ua.com.fielden.platform.client.svg.combining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class SvgCombiningUtilityTest {

    @Test
    public void test_for_extract_file_contetn() throws Exception {

        final String file  = "src/test/resources/icon.svg";
        final String content = Files.toString(new File(file), Charsets.UTF_8);
        assertEquals(SvgCombiningUtility.getContentOfSvgFiles(file), content);

    }

    @Test
    public void output_file_can_not_be_empty_after_cmbining() throws Exception {

        final String args[] = new String[3];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        args[2] = "src/test/resources/combiningSvgResult.html";

        SvgCombiningUtility.combineSvgFilesContent(args, 1000, "name");
        assertFalse(args[2].isEmpty());

    }

    @Test
    public void file_wich_does_not_exist_can_not_be_argument() throws IOException, URISyntaxException {

        final String args[] = new String[3];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        args[2] = "src/test/resources/notExistedFile.html";

        try {
            SvgCombiningUtility.combineSvgFilesContent(args, 1000, "name");
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Dest file does not exist!", ex.getMessage());
        }

    }

}
