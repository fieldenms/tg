package ua.com.fielden.platform.client.svg.combining;

import org.junit.Test;

public class SvgCombiningUtilityTest {

    @Test
    public void test_to_copy() throws Exception {

        final String args[] = new String[2];
        args[0] = "/home/natalie/Pictures/fleet-icons/organisational.svg";
        args[1] = "/home/natalie/Pictures/fleet-icons/dest.html";

        SvgCombiningUtility.main(args);

    }

}
