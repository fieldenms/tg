package ua.com.fielden.platform.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.security.SecurityTokenInfo.isSuperTokenOf;
import static ua.com.fielden.platform.security.SecurityTokenInfo.isTopLevel;
import static ua.com.fielden.platform.security.SecurityTokenInfo.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfo.shortDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfo.superToken;

import org.junit.Test;

/**
 * Ensures correct behaviour of the static class {@link SecurityTokenInfo}.
 * 
 * @author 01es
 * 
 */
public class SecurityTokenInfoTest {

    @Test
    public void testThatShortDescIsDeterminedCorrectly() {
        assertEquals("Incorrect short desc for top level.", "TopLevelSecurityToken", shortDesc(TopLevelSecurityToken.class));
        assertEquals("Incorrect short desc for lower level.", "LowerLevelSecurityToken", shortDesc(LowerLevelSecurityToken.class));
        try {
            shortDesc(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void testThatLongDescIsDeterminedCorrectly() {
        assertEquals("Incorrect short desc for top level.", "Top level security token used for testing purposes.", longDesc(TopLevelSecurityToken.class));
        assertEquals("Incorrect short desc for lower level.", "Lower level security token used for testing purposes.", longDesc(LowerLevelSecurityToken.class));
        try {
            shortDesc(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void testThatTopLevelTokenIsDistinguishedFromLowerOne() {
        assertTrue("Failed to determined top level token.", isTopLevel(TopLevelSecurityToken.class));
        assertFalse("Failed to determined top level token.", isTopLevel(LowerLevelSecurityToken.class));
        try {
            isTopLevel(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void testThatSuperTokenIsDeterminedCorrectly() {
        assertEquals("Incorrectly determined top level token.", ISecurityToken.class, superToken(TopLevelSecurityToken.class));
        assertEquals("Incorrectly determined top level token.", TopLevelSecurityToken.class, superToken(LowerLevelSecurityToken.class));
        try {
            superToken(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void testThatSuperTokenTestWorks() {
        assertTrue("Failed to test super token.", isSuperTokenOf(ISecurityToken.class, TopLevelSecurityToken.class));
        assertFalse("Failed to test super token.", isSuperTokenOf(LowerLevelSecurityToken.class, TopLevelSecurityToken.class));
        assertTrue("Failed to test super token.", isSuperTokenOf(TopLevelSecurityToken.class, LowerLevelSecurityToken.class));
        try {
            isSuperTokenOf(TopLevelSecurityToken.class, MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Exception ex) {
        }
    }
}
