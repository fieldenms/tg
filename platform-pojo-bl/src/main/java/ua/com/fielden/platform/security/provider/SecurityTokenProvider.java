package ua.com.fielden.platform.security.provider;

import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.isSuperTokenOf;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.isTopLevel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.attachment.AttachmentDownload_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRoleTokensUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRolesUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;

/**
 * Searches for all available security tokens in the application based on the provided path and package name. The result is presented as as a tree-like structure containing all
 * tokens with correctly determined association between them.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenProvider {
    /**
     * Contains top level security token nodes.
     */
    private final SortedSet<SecurityTokenNode> topLevelSecurityTokenNodes;

    /**
     * Creates security provider by automatically determining all security tokens available on the path within the specified package. May throw an exception as a result of failure
     * to loaded token classes.
     * 
     * @param path
     *            -- a path to classes or a jar (requires jar file name too) where security tokens are located.
     * @param packageName
     *            -- a package name containing security tokens (sub-packages are traversed automatically).
     * @throws Exception
     */
    public SecurityTokenProvider(final String path, final String packageName) {
        final List<Class<? extends ISecurityToken>> allTokens = ClassesRetriever.getAllClassesInPackageDerivedFrom(path, packageName, ISecurityToken.class);
        allTokens.add(User_CanSave_Token.class);
        allTokens.add(User_CanDelete_Token.class);
        allTokens.add(UserRole_CanSave_Token.class);
        allTokens.add(UserRole_CanDelete_Token.class);
        allTokens.add(UserRolesUpdater_CanExecute_Token.class);
        allTokens.add(UserRoleTokensUpdater_CanExecute_Token.class);
        allTokens.add(Attachment_CanSave_Token.class);
        allTokens.add(Attachment_CanDelete_Token.class);
        allTokens.add(AttachmentDownload_CanExecute_Token.class);

        topLevelSecurityTokenNodes = buildTokenNodes(allTokens);
    }

    private SortedSet<SecurityTokenNode> buildTokenNodes(final List<Class<? extends ISecurityToken>> allTokens) {
        final SortedSet<SecurityTokenNode> topTokenNodes = new TreeSet<>();
        // iterate over all tokens and determine top level tokens
        // add them to a separate list and remove from a list over which iteration occurs
        for (final Iterator<Class<? extends ISecurityToken>> iter = allTokens.iterator(); iter.hasNext();) {
            final Class<? extends ISecurityToken> token = iter.next();
            if (isTopLevel(token)) {
                topTokenNodes.add(new SecurityTokenNode(token));
                iter.remove();
            }
        }
        // iterate over the top token nodes and recursively builds all sub token nodes
        for (final Iterator<SecurityTokenNode> iter = topTokenNodes.iterator(); iter.hasNext() && !allTokens.isEmpty();) {
            digg(iter.next(), allTokens); // allTokens is mutated by digg
        }

        return topTokenNodes;
    }

    /**
     * Determines and creates token nodes representing sub tokens for the passed in current token node.
     * 
     * @param superTokenNode
     *            -- current token node for which sub tokens are being searched
     * @param remainingTokens
     *            -- a list of remaining tokens used for search; all found sub tokens are removed from this list.
     */
    private void digg(final SecurityTokenNode superTokenNode, final List<Class<? extends ISecurityToken>> remainingTokens) {
        final SortedSet<SecurityTokenNode> toBeRemoved = new TreeSet<>();
        // find all direct sub tokens of the current super token
        for (final Iterator<Class<? extends ISecurityToken>> iter = remainingTokens.iterator(); iter.hasNext();) {
            final Class<? extends ISecurityToken> token = iter.next();
            if (isSuperTokenOf(superTokenNode.getToken(), token)) {
                toBeRemoved.add(new SecurityTokenNode(token, superTokenNode));
                // remove the found sub-token from the list of remaining tokens to reduce the number of items used further in the search
                iter.remove();
            }
        }

        // recursively find all sub tokens of just found sub tokens
        for (final SecurityTokenNode node : toBeRemoved) {
            digg(node, remainingTokens);
        }
    }

    public SortedSet<SecurityTokenNode> getTopLevelSecurityTokenNodes() {
        return Collections.unmodifiableSortedSet(topLevelSecurityTokenNodes);
    }
}
