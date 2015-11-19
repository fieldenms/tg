package ua.com.fielden.platform.client.svg.combining;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SvgCombiningUtilityTest {

    @Test
    public void test_to_copy() throws Exception {

        final String args[] = new String[2];
        args[0] = "/home/natalie/Pictures/fleet-icons/user.svg";
        args[1] = "/home/natalie/Pictures/fleet-icons/dest.html";

        SvgCombiningUtility.main(args);
        assertFalse(args[1].isEmpty());

    }

    @Test
    public void file_wich_does_not_exist_can_not_be_argument() {

        final String args[] = new String[2];
        args[0] = "/home/natalie/Pictures/fleet-icons/user.svg";
        args[1] = "/home/natalie/Pictures/fleet-icons/d.html";

        try {
            SvgCombiningUtility.main(args);
            fail();
        } catch (final Exception e) {
        }

    }

}
