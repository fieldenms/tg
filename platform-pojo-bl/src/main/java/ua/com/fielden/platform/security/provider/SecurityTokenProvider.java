package ua.com.fielden.platform.security.provider;

import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.isSuperTokenOf;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.isTopLevel;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.tokens.attachment.AttachmentDownload_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanRead_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanSave_Token;
import ua.com.fielden.platform.security.tokens.persistent.KeyNumber_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.KeyNumber_CanRead_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserRoleTokensUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRolesUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;
import ua.com.fielden.platform.security.tokens.web_api.GraphiQL_CanExecute_Token;
import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * Searches for all available security tokens in the application based on the provided path and package name.
 * The result is presented as as a tree-like structure containing all tokens with correctly determined association between them.
 * <p>
 * <b>A fundamental assumption:</b> simple class names uniquely identify security tokens and entities!
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenProvider implements ISecurityTokenProvider {
    public static final String ERR_DUPLICATE_SECURITY_TOKENS = "Not all security tokens are unique in their simple class name. This is required.";

    /** 
     * A map between token classes and their names. 
     * Used as a cache for obtaining class by name. 
     */
    private final Map<String, Class<? extends ISecurityToken>> tokenClassesByName = new HashMap<>();
    private final Map<String, Class<? extends ISecurityToken>> tokenClassesBySimpleName = new HashMap<>();

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
    @Inject
    public SecurityTokenProvider(
            final @Named("tokens.path") String path,
            final @Named("tokens.package") String packageName
    ) {
        final Set<Class<? extends ISecurityToken>> platformLevelTokens = CollectionUtil.setOf(
                User_CanSave_Token.class,
                User_CanRead_Token.class,
                User_CanReadModel_Token.class,
                User_CanDelete_Token.class,
                UserRole_CanSave_Token.class,
                UserRole_CanRead_Token.class,
                UserRole_CanReadModel_Token.class,
                UserRole_CanDelete_Token.class,
                UserAndRoleAssociation_CanRead_Token.class,
                UserAndRoleAssociation_CanReadModel_Token.class,
                UserRolesUpdater_CanExecute_Token.class,
                UserRoleTokensUpdater_CanExecute_Token.class,
                Attachment_CanSave_Token.class,
                Attachment_CanRead_Token.class,
                Attachment_CanReadModel_Token.class,
                Attachment_CanDelete_Token.class,
                AttachmentDownload_CanExecute_Token.class,
                DomainExplorer_CanRead_Token.class,
                DomainExplorer_CanReadModel_Token.class,
                KeyNumber_CanRead_Token.class,
                KeyNumber_CanReadModel_Token.class,
                GraphiQL_CanExecute_Token.class);
        final Set<Class<? extends ISecurityToken>> allTokens = new HashSet<>(ClassesRetriever.getAllClassesInPackageDerivedFrom(path, packageName, ISecurityToken.class));
        allTokens.addAll(platformLevelTokens);

        allTokens.forEach(type -> { tokenClassesByName.put(type.getName(), type); tokenClassesBySimpleName.put(type.getSimpleName(), type); });
        if (tokenClassesByName.size() != tokenClassesBySimpleName.size()) {
            throw new SecurityException(ERR_DUPLICATE_SECURITY_TOKENS);
        }
        topLevelSecurityTokenNodes = buildTokenNodes(allTokens);
    }

    @Override
    public SortedSet<SecurityTokenNode> getTopLevelSecurityTokenNodes() {
        return Collections.unmodifiableSortedSet(topLevelSecurityTokenNodes);
    }

    /**
     * Returns a class representing a security token by its simple or full class name.
     *
     * @param tokenClassName -- a simple or a full class name for a security token.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISecurityToken> Optional<Class<T>> getTokenByName(final String tokenClassName) {
        final Class<T> classBySimpleName = (Class<T>) tokenClassesBySimpleName.get(tokenClassName);
        return Optional.ofNullable(classBySimpleName != null ? classBySimpleName : (Class<T>) tokenClassesByName.get(tokenClassName));
    }

    private SortedSet<SecurityTokenNode> buildTokenNodes(final Set<Class<? extends ISecurityToken>> allTokens) {
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
    private void digg(final SecurityTokenNode superTokenNode, final Set<Class<? extends ISecurityToken>> remainingTokens) {
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

}