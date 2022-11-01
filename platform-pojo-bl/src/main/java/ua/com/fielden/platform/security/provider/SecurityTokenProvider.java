package ua.com.fielden.platform.security.provider;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.shortDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import ua.com.fielden.platform.security.tokens.open_simple_master.AttachmentMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.DashboardRefreshFrequencyMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserRoleMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequencyUnit_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequencyUnit_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanSave_Token;
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
     * The "default" constructor that can be used by IoC.
     */
    @Inject
    public SecurityTokenProvider(
            final @Named("tokens.path") String path,
            final @Named("tokens.package") String packageName
    ) {
        this(path, packageName, emptySet(), emptySet());
    }

    /**
     * Creates security provider by automatically determining all security tokens available on the path within the specified package.
     * May throw an exception as a result of failure to loaded token classes.
     *
     * @param path -- a path to classes or a jar (requires jar file name too) where security tokens are located.
     * @param packageName -- a package name containing security tokens (sub-packages are traversed automatically).
     * @param extraTokens -- additional tokens that belong neither to the standard platform ones not to the application specific ones, loaded dynamically "tokens.path".
     * @param redundantTokens -- tokens to be removed for consideration; in most cases this would only be relevant for some of the platform-level tokens that are not applicable for some applications.
     */
    public SecurityTokenProvider(
            final String path,
            final String packageName,
            final Set<Class<? extends ISecurityToken>> extraTokens,
            final Set<Class<? extends ISecurityToken>> redundantTokens
    ) {
        long start = System.currentTimeMillis();
        final Set<Class<? extends ISecurityToken>> platformLevelTokens = CollectionUtil.setOf(
                User_CanSave_Token.class,
                User_CanRead_Token.class,
                User_CanReadModel_Token.class,
                User_CanDelete_Token.class,
                UserMaster_CanOpen_Token.class,
                UserRole_CanSave_Token.class,
                UserRole_CanRead_Token.class,
                UserRole_CanReadModel_Token.class,
                UserRole_CanDelete_Token.class,
                UserRoleMaster_CanOpen_Token.class,
                UserAndRoleAssociation_CanRead_Token.class,
                UserAndRoleAssociation_CanReadModel_Token.class,
                UserRolesUpdater_CanExecute_Token.class,
                UserRoleTokensUpdater_CanExecute_Token.class,
                Attachment_CanSave_Token.class,
                Attachment_CanRead_Token.class,
                Attachment_CanReadModel_Token.class,
                Attachment_CanDelete_Token.class,
                AttachmentMaster_CanOpen_Token.class,
                AttachmentDownload_CanExecute_Token.class,
                DashboardRefreshFrequencyUnit_CanRead_Token.class,
                DashboardRefreshFrequencyUnit_CanReadModel_Token.class,
                DashboardRefreshFrequency_CanSave_Token.class,
                DashboardRefreshFrequency_CanRead_Token.class,
                DashboardRefreshFrequency_CanReadModel_Token.class,
                DashboardRefreshFrequency_CanDelete_Token.class,
                DashboardRefreshFrequencyMaster_CanOpen_Token.class,
                DomainExplorer_CanRead_Token.class,
                DomainExplorer_CanReadModel_Token.class,
                KeyNumber_CanRead_Token.class,
                KeyNumber_CanReadModel_Token.class,
                GraphiQL_CanExecute_Token.class);
        final Set<Class<? extends ISecurityToken>> allTokens = new HashSet<>(ClassesRetriever.getAllClassesInPackageDerivedFrom(path, packageName, ISecurityToken.class));
        allTokens.addAll(platformLevelTokens);
        allTokens.addAll(extraTokens);
        allTokens.removeAll(redundantTokens);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Token List Creation: " + timeElapsed);
        System.out.println("Number Of Tokens To Load: " + allTokens.size());
        start = System.currentTimeMillis();
        allTokens.forEach(type -> { tokenClassesByName.put(type.getName(), type); tokenClassesBySimpleName.put(type.getSimpleName(), type); });
        if (tokenClassesByName.size() != tokenClassesBySimpleName.size()) {
            throw new SecurityException(ERR_DUPLICATE_SECURITY_TOKENS);
        }
        topLevelSecurityTokenNodes = buildTokenNodes(allTokens);
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println("Token map and tree creation: " + timeElapsed);
    }

    @Override
    public SortedSet<SecurityTokenNode> getTopLevelSecurityTokenNodes() {
        return Collections.unmodifiableSortedSet(topLevelSecurityTokenNodes);
    }

    /**
     * Returns a class representing a security token by its simple or full class name.
     *
     * @param tokenClassSimpleName -- a simple or a full class name for a security token.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISecurityToken> Optional<Class<T>> getTokenByName(final String tokenClassSimpleName) {
        final Class<T> classBySimpleName = (Class<T>) tokenClassesBySimpleName.get(tokenClassSimpleName);
        return ofNullable(classBySimpleName != null ? classBySimpleName : (Class<T>) tokenClassesByName.get(tokenClassSimpleName));
    }

    private SortedSet<SecurityTokenNode> buildTokenNodes(final Set<Class<? extends ISecurityToken>> allTokens) {
        final Map<Class<? extends ISecurityToken>, SecurityTokenNode> topTokenNodes = new HashMap<>();

        allTokens.forEach(token -> {
            final List<Class<? extends ISecurityToken>> tokenHierarchy = genHierarchyPath(token);
            tokenHierarchy.stream().reduce((SecurityTokenNode)null, (tokenNode, tokenClass) -> {
                SecurityTokenNode nextNode = tokenNode == null ? topTokenNodes.get(tokenClass) : tokenNode.getSubTokenNode(tokenClass.getName());
                if (nextNode == null) {
                    nextNode = new SecurityTokenNode(tokenClass.getName(), shortDesc(token), longDesc(token));
                    if (tokenNode == null) {
                        topTokenNodes.put(tokenClass, nextNode);
                    } else {
                        tokenNode.add(nextNode);
                    }
                }
                return nextNode;
            }, (prev, next) -> next);
        });

        return new TreeSet<>(topTokenNodes.values());
    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends ISecurityToken>> genHierarchyPath(final Class<? extends ISecurityToken> token) {
        final List<Class<? extends ISecurityToken>> tokenHierarchyList = new ArrayList<>();
        Class<?> parentNode = token;
        while (ISecurityToken.class.isAssignableFrom(parentNode)) {
            tokenHierarchyList.add((Class<? extends ISecurityToken>)parentNode);
            parentNode = parentNode.getSuperclass();
        }
        Collections.reverse(tokenHierarchyList);
        return tokenHierarchyList;
    }

}