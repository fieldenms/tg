package ua.com.fielden.platform.security.provider;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.shortDesc;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.Iterator;
import java.util.SortedSet;

import org.junit.Test;

import ua.com.fielden.platform.security.tokens.open_simple_master.UserMaster_CanOpen_Token;
import ua.com.fielden.security.tokens.open_compound_master.OpenUserMasterAction_CanOpen_Token;

/**
 * A test case to ensure correct configuration of security tokens as provided for security tooling (the tree structure).
 *
 * @author TG Team
 *
 */
public class SecurityTokenProviderAndNodeConstructionTest {

    @Test
    public void the_default_type_hierarchy_based_association_between_tokens_does_not_have_to_be_followed() {
        final SecurityTokenNode superNode = new SecurityTokenNode(Top1LevelSecurityToken.class);
        assertEquals("Incorrect token.", Top1LevelSecurityToken.class, superNode.getToken());
        assertEquals("Incorrect short desc.", shortDesc(Top1LevelSecurityToken.class), superNode.getShortDesc());
        assertEquals("Incorrect long desc.", longDesc(Top1LevelSecurityToken.class), superNode.getLongDesc());
        assertNull("Incorrect super node.", superNode.getSuperTokenNode());
        assertEquals("Incorrect number of sub tokens", 0, superNode.getSubTokenNodes().size());

        try {
            new SecurityTokenNode(Lower1LevelSecurityToken.class);
            fail("A sub token node at incorrect state was allowed to be created.");
        } catch (final Exception e) {
        }

        final SecurityTokenNode subNode = new SecurityTokenNode(Lower1LevelSecurityToken.class, superNode);

        final SecurityTokenNode subSubNode = new SecurityTokenNode(Top1LevelSecurityToken.class, subNode);

        assertEquals("Incorrect token.", Lower1LevelSecurityToken.class, subNode.getToken());
        assertEquals("Incorrect short desc.", shortDesc(Lower1LevelSecurityToken.class), subNode.getShortDesc());
        assertEquals("Incorrect long desc.", longDesc(Lower1LevelSecurityToken.class), subNode.getLongDesc());
        assertEquals("Incorrect super node.", superNode, subNode.getSuperTokenNode());
        assertEquals("Incorrect number of sub tokens", 1, subNode.getSubTokenNodes().size());
        assertEquals("Incorrect number of sub tokens", 1, superNode.getSubTokenNodes().size());
        assertEquals("Incorrect super node.", subNode, subSubNode.getSuperTokenNode());
    }

    @Test
    public void security_token_hierarchy_is_determined_correctly_for_the_specified_path_and_package() {
        final SecurityTokenProvider provider = new SecurityTokenProvider("target/test-classes", "ua.com.fielden.platform.security.provider");
        final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
        assertEquals("Incorrect number of top security tokens.", 37, topNodes.size());

        // skip attachment related security tokens before getting iterator nodesWithSkippedAttachmentTokens
        final Iterator<SecurityTokenNode> superIter = topNodes.stream().skip(18).collect(toList()).iterator();

        final SecurityTokenNode top1 = superIter.next();
        assertEquals("Incorrect first top token.", Top1LevelSecurityToken.class, top1.getToken());
        assertEquals("Incorrect number of sub tokens.", 2, top1.getSubTokenNodes().size());
        final Iterator<SecurityTokenNode> subIter = top1.getSubTokenNodes().iterator();
        assertEquals("Incorrect sub token.", Lower1LevelSecurityToken.class, subIter.next().getToken());
        assertEquals("Incorrect sub token.", Lower2LevelSecurityToken.class, subIter.next().getToken());

        final SecurityTokenNode top2 = superIter.next();
        assertEquals("Incorrect second top token.", Top2LevelSecurityToken.class, top2.getToken());
        assertEquals("Incorrect number of sub tokens.", 0, top2.getSubTokenNodes().size());
    }

    @Test
    public void security_token_hierarchy_cannot_be_build_if_tokens_have_duplicate_simple_class_name() {
        try {
            new SecurityTokenProvider("target/test-classes", "ua.com.fielden.platform.security");
            fail("Security token provider did not detect duplicate tokens.");
        } catch (final ua.com.fielden.platform.security.exceptions.SecurityException ex) {
            assertEquals(SecurityTokenProvider.ERR_DUPLICATE_SECURITY_TOKENS, ex.getMessage());
        }
    }

    @Test
    public void security_token_provider_can_be_used_to_exclude_standard_tokens() {
        final SecurityTokenProvider standardProvider = new SecurityTokenProvider("target/test-classes", "ua.com.fielden.platform.security.provider");
        assertTrue(standardProvider.getTokenByName(UserMaster_CanOpen_Token.class.getSimpleName()).isPresent());
        final SecurityTokenProvider providerWithExclution = new SecurityTokenProvider("target/test-classes", "ua.com.fielden.platform.security.provider", setOf(), setOf(UserMaster_CanOpen_Token.class));
        assertFalse(providerWithExclution.getTokenByName(UserMaster_CanOpen_Token.class.getSimpleName()).isPresent());
    }

    @Test
    public void security_token_provider_can_be_used_to_include_extra_tokens() {
        final SecurityTokenProvider standardProvider = new SecurityTokenProvider("target/test-classes", "ua.com.fielden.platform.security.provider");
        assertFalse(standardProvider.getTokenByName(OpenUserMasterAction_CanOpen_Token.class.getSimpleName()).isPresent());
        final SecurityTokenProvider providerWithExclution = new SecurityTokenProvider("target/test-classes", "ua.com.fielden.platform.security.provider", setOf(OpenUserMasterAction_CanOpen_Token.class), setOf());
        assertTrue(providerWithExclution.getTokenByName(OpenUserMasterAction_CanOpen_Token.class.getSimpleName()).isPresent());
    }

}