package ua.com.fielden.platform.client.svg.combining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

public class SvgCombiningUtilityTest {

    @Test
    public void test_for_copy() throws Exception {

        final String args[] = new String[3];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        args[2] = "src/test/resources/combiningSvgResult.html";

        SvgCombiningUtility.svgCombining(args);
        assertFalse(args[1].isEmpty());

    }

    @Test
    public void file_wich_does_not_exist_can_not_be_argument() throws IOException, URISyntaxException {

        final String args[] = new String[3];
        args[0] = "src/test/resources/icon.svg";
        args[1] = "src/test/resources/icon2.svg";
        args[2] = "src/test/resources/notExistedFile.html";

        try {
            SvgCombiningUtility.svgCombining(args);
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Dest file does not exist!", ex.getMessage());
        }

    }

}
