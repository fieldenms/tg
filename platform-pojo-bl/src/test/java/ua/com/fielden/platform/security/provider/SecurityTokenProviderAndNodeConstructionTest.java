package ua.com.fielden.platform.security.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.security.SecurityTokenInfo.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfo.shortDesc;

import java.util.Iterator;
import java.util.SortedSet;

import org.junit.Test;

/**
 * A test case to ensure correct configuration of security tokens as provided for security tooling (the tree structure).
 * 
 * @author 01es
 * 
 */
public class SecurityTokenProviderAndNodeConstructionTest {

    @Test
    public void testThatSecurityTokenNodeIsConstructedCorrectly() {
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

        try {
            new SecurityTokenNode(Top1LevelSecurityToken.class, subNode);
            fail("A super token node at incorrect state was allowed to be created.");
        } catch (final Exception e) {
        }

        assertEquals("Incorrect token.", Lower1LevelSecurityToken.class, subNode.getToken());
        assertEquals("Incorrect short desc.", shortDesc(Lower1LevelSecurityToken.class), subNode.getShortDesc());
        assertEquals("Incorrect long desc.", longDesc(Lower1LevelSecurityToken.class), subNode.getLongDesc());
        assertEquals("Incorrect super node.", superNode, subNode.getSuperTokenNode());
        assertEquals("Incorrect number of sub tokens", 0, subNode.getSubTokenNodes().size());
        assertEquals("Incorrect number of sub tokens", 1, superNode.getSubTokenNodes().size());
    }

    @Test
    public void testThatSecurityTokenHierarchyIsDeterminedCorrectly() throws Exception {
        final SecurityTokenProvider provider = new SecurityTokenProvider("target/test-classes", "ua.com.fielden.platform.security.provider");
        final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
        assertEquals("Incorrect number of top security tokens.", 2, topNodes.size());

        final Iterator<SecurityTokenNode> superIter = topNodes.iterator();

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
}
