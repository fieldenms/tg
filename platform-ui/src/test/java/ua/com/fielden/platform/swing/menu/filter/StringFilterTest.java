package ua.com.fielden.platform.swing.menu.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test case for {@link StringFilter} covering both single matching criterion and multiple criteria.
 * 
 * @author 01es
 */
public class StringFilterTest {

    @Test
    public void testSinglePatternMatching() {
        final StringFilter flt = new StringFilter();
        final String value = "scala rocks";

        assertFalse("Should match, and thus not filter.", flt.filter(value, "*"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "scala rocks"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "sca"));
        assertTrue("Should not match, and thus filter.", flt.filter(value, "something else"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "scala *"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "*rocks"));
        assertTrue("Should match, and thus filter.", flt.filter(value, "*roc"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "*sca*ro*"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "sca*ro*"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "*ca*rocks"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, ""));
    }

    @Test
    public void testMultiplePatternMatching() {
        final StringFilter flt = new StringFilter();
        final String value = "scala rocks";

        assertFalse("Should match, and thus not filter.", flt.filter(value, "*,*"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "*, scala rocks"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "scala rocks, *"));
        assertTrue("Should not match, and thus filter.", flt.filter(value, "something else, else, else too"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "something else, scala rocks"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "something else, *"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "something else, *, else"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "scala *, something else"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "something else, *rocks"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "something else, *sca*ro*, else"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "sca*ro*, something else"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "something else, *ca*rocks, *"));
        assertFalse("Should match, and thus not filter.", flt.filter(value, "something else, "));
    }
}
