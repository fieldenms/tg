package ua.com.fielden.platform.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.isSuperTokenOf;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.isTopLevel;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.shortDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.superToken;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;

/**
 * Ensures correct behaviour of the static class {@link SecurityTokenInfoUtils}.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenInfoUtilsTest {

    @Test
    public void short_desc_can_be_determined_for_token_annotated_with_KeyTitle_but_no_TITLE_and_DESC_fields() {
        assertEquals("Incorrect short desc for top level.", "TopLevelSecurityToken", shortDesc(TopLevelSecurityToken.class));
        assertEquals("Incorrect short desc for lower level.", "LowerLevelSecurityToken", shortDesc(LowerLevelSecurityToken.class));
    }

    @Test
    public void short_desc_fails_for_token_with_neither_annotation_KeyTitle_nor_TITLE_and_DESC_fields() {
        try {
            shortDesc(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Result ex) {
            assertEquals(String.format(SecurityTokenInfoUtils.ERR_MELFORMED_TOKEN, MalformedSecurityToken.class.getName()), ex.getMessage());
        }
    }

    @Test
    public void long_desc_can_be_determined_for_token_annotated_with_KeyTitle_but_no_TITLE_and_DESC_fields() {
        assertEquals("Incorrect short desc for top level.", "Top level security token used for testing purposes.", longDesc(TopLevelSecurityToken.class));
        assertEquals("Incorrect short desc for lower level.", "Lower level security token used for testing purposes.", longDesc(LowerLevelSecurityToken.class));
    }

    @Test
    public void long_desc_fails_for_token_with_neither_annotation_KeyTitle_nor_TITLE_and_DESC_fields() {
        try {
            longDesc(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Result ex) {
            assertEquals(String.format(SecurityTokenInfoUtils.ERR_MELFORMED_TOKEN, MalformedSecurityToken.class.getName()), ex.getMessage());
        }
    }
    
    
    @Test
    public void top_and_nested_level_tokens_can_be_correctly_identified() {
        assertTrue("Failed to determined top level token.", isTopLevel(TopLevelSecurityToken.class));
        assertFalse("Failed to determined top level token.", isTopLevel(LowerLevelSecurityToken.class));
    }

    @Test
    public void determining_the_level_for_melformed_tokens_result_in_error() {
        try {
            isTopLevel(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Result ex) {
            assertEquals(String.format(SecurityTokenInfoUtils.ERR_MELFORMED_TOKEN, MalformedSecurityToken.class.getName()), ex.getMessage());
        }
    }

    
    @Test
    public void super_tokens_can_be_identified_for_both_top_and_nested_level_of_tokens() {
        assertEquals("Incorrectly determined top level token.", ISecurityToken.class, superToken(TopLevelSecurityToken.class));
        assertEquals("Incorrectly determined top level token.", TopLevelSecurityToken.class, superToken(LowerLevelSecurityToken.class));
    }

    @Test
    public void deterimining_super_token_for_melformed_tokens_result_in_error() {
        try {
            superToken(MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Result ex) {
            assertEquals(String.format(SecurityTokenInfoUtils.ERR_MELFORMED_TOKEN, MalformedSecurityToken.class.getName()), ex.getMessage());
        }
    }

    @Test
    public void super_token_assertion_corretly_works_well_formed_tokens() {
        assertTrue("Failed to test super token.", isSuperTokenOf(ISecurityToken.class, TopLevelSecurityToken.class));
        assertFalse("Failed to test super token.", isSuperTokenOf(LowerLevelSecurityToken.class, TopLevelSecurityToken.class));
        assertTrue("Failed to test super token.", isSuperTokenOf(TopLevelSecurityToken.class, LowerLevelSecurityToken.class));
    }

    @Test
    public void super_token_assertion_throw_exception_for_melformed_tokens() {
        try {
            isSuperTokenOf(TopLevelSecurityToken.class, MalformedSecurityToken.class);
            fail("Should have determined that token is malformed.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void short_and_long_descs_can_be_determined_for_token_without_KeyTitle_but_with_TITLE_and_DESC_fields() {
        assertEquals(TopLevelSecurityTokenWithTitleAndDescFields.TITLE, shortDesc(TopLevelSecurityTokenWithTitleAndDescFields.class));
        assertEquals(TopLevelSecurityTokenWithTitleAndDescFields.DESC, longDesc(TopLevelSecurityTokenWithTitleAndDescFields.class));
    }

    @Test
    public void super_relationships_are_correctly_identified_for_tokens_without_KeyTitle_but_with_TITLE_and_DESC_fields() {
        assertTrue(isTopLevel(TopLevelSecurityTokenWithTitleAndDescFields.class));
        assertEquals(ISecurityToken.class, superToken(TopLevelSecurityTokenWithTitleAndDescFields.class));
        assertTrue(isSuperTokenOf(ISecurityToken.class, TopLevelSecurityTokenWithTitleAndDescFields.class));
    }

}
