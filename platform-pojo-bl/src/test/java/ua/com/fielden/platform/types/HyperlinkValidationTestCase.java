package ua.com.fielden.platform.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.types.exceptions.ValueObjectException;

public class HyperlinkValidationTestCase {

    @Test
    public void valid_http_link_is_supported() {
        final String strLink = "http://www.domain.com";
        final Hyperlink link = new Hyperlink(strLink);
        assertEquals(strLink, link.value);

        final String strLinkUpper = "HTTP://www.domain.com";
        final Hyperlink linkUpper = new Hyperlink(strLinkUpper);
        assertEquals(strLinkUpper, linkUpper.value);
    }

    @Test(expected = ValueObjectException.class)
    public void mistyped_http_link_is_not_supported() {
        new Hyperlink("http:/www.domain.com");
    }

    @Test
    public void valid_https_link_is_supported() {
        final String strLink = "https://www.domain.com";
        final Hyperlink link = new Hyperlink(strLink);
        assertEquals(strLink, link.value);

        final String strLinkUpper = "HTTPS://www.domain.com";
        final Hyperlink linkUpper = new Hyperlink(strLinkUpper);
        assertEquals(strLinkUpper, linkUpper.value);
    }

    @Test(expected = ValueObjectException.class)
    public void mistyped_https_link_is_not_supported() {
        new Hyperlink("https//www.domain.com");
    }

    @Test
    public void valid_ftp_link_is_supported() {
        final String strLink = "ftp://www.domain.com";
        final Hyperlink link = new Hyperlink(strLink);
        assertEquals(strLink, link.value);

        final String strLinkUpper = "FTP://www.domain.com";
        final Hyperlink linkUpper = new Hyperlink(strLinkUpper);
        assertEquals(strLinkUpper, linkUpper.value);
    }

    @Test(expected = ValueObjectException.class)
    public void mistyped_ftp_link_is_not_supported() {
        new Hyperlink("ftp://-www.domain.com");
    }

    @Test
    public void valid_ftps_link_is_supported() {
        final String strLink = "ftps://www.domain.com";
        final Hyperlink link = new Hyperlink(strLink);
        assertEquals(strLink, link.value);

        final String strLinkUpper = "FTPS://www.domain.com";
        final Hyperlink linkUpper = new Hyperlink(strLinkUpper);
        assertEquals(strLinkUpper, linkUpper.value);
    }

    @Test(expected = ValueObjectException.class)
    public void mistyped_ftps_link_is_not_supported() {
        new Hyperlink("ftps://-www.domain.com");
    }

    @Test
    public void valid_mailto_link_is_supported() {
        final String strLink = "mailto:name1@domain.com,name2@domain.com";
        final Hyperlink link = new Hyperlink(strLink);
        assertEquals(strLink, link.value);
        final String strLinkUpper = "MAILTO:name1@domain.com,name2@domain.com";
        final Hyperlink linkUpper = new Hyperlink(strLinkUpper);
        assertEquals(strLinkUpper, linkUpper.value);
    }

    @Test(expected = ValueObjectException.class)
    public void mistyped_mailto_link_is_not_supported() {
        new Hyperlink("mailto name@domain.com");
    }

    @Test
    public void localhost_link_is_supported() {
        final String strLink = "http://localhost";
        final Hyperlink link = new Hyperlink(strLink);
        assertEquals(strLink, link.value);
    }
}
