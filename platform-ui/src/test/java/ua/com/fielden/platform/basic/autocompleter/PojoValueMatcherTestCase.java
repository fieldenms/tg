/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link PojoValueMatcher} class
 * 
 * @author Yura, Oleh
 */
public class PojoValueMatcherTestCase {

    @Test
    public void testValueMatchesPatternMethod() {
        Assert.assertTrue(PojoValueMatcher.valueMatchesPattern("AFT", "A"));
        Assert.assertTrue(PojoValueMatcher.valueMatchesPattern("AFT", "AFT"));
        Assert.assertTrue(PojoValueMatcher.valueMatchesPattern("AFT", "AFT*"));
        Assert.assertTrue(PojoValueMatcher.valueMatchesPattern("AFTx", "AFT*"));

        Assert.assertFalse(PojoValueMatcher.valueMatchesPattern("AFT", "V"));
        Assert.assertFalse(PojoValueMatcher.valueMatchesPattern("AFT", "AFT1"));
    }

}
