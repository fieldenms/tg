package ua.com.fielden.platform.security.provider;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ua.com.fielden.platform.audit.AuditUtils;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.AuditModuleToken;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.tokens.ISecurityTokenGenerator;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.tokens.attachment.*;
import ua.com.fielden.platform.security.tokens.functional.PersistentEntityInfo_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.AttachmentMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.DashboardRefreshFrequencyMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserRoleMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.persistent.*;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.*;
import ua.com.fielden.platform.security.tokens.web_api.GraphiQL_CanExecute_Token;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.collections4.CollectionUtils.disjunction;

/// Tokens are accumulated from the following sources:
/// - The location of security tokens given by application properties `tokens.path` and `tokens.package` is scanned.
/// - Tokens for synthetic audit-entity types are dynamically generated in package `${tokens.package}.audit`.
///   Also see [#templatesForAuditedType(Class)].
///
/// **A fundamental assumption:** simple class names uniquely identify security tokens and entities.
///
/// @author TG Team
///
@Singleton
public class SecurityTokenProvider implements ISecurityTokenProvider {

    /**
     * A map between token classes and their names.
     * Used as a cache for obtaining class by name.
     */
    private final Map<String, Class<? extends ISecurityToken>> tokenClassesByName = new HashMap<>();
    private final Map<String, Class<? extends ISecurityToken>> tokenClassesBySimpleName = new HashMap<>();

    /**
     * Contains top level security token nodes.
     * Effectively final.
     */
    private SortedSet<SecurityTokenNode> topLevelSecurityTokenNodes;

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
        final Set<Class<? extends ISecurityToken>> platformLevelTokens = CollectionUtil.setOf(
                User_CanSave_Token.class,
                User_CanRead_Token.class,
                User_CanReadModel_Token.class,
                ReUser_CanRead_Token.class,
                ReUser_CanReadModel_Token.class,
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
                GraphiQL_CanExecute_Token.class,
                UserDefinableHelp_CanSave_Token.class,
                PersistentEntityInfo_CanExecute_Token.class,
                AuditModuleToken.class);
        final Set<Class<? extends ISecurityToken>> allTokens = new HashSet<>(ClassesRetriever.getAllClassesInPackageDerivedFrom(path, packageName, ISecurityToken.class));
        allTokens.addAll(platformLevelTokens);
        allTokens.addAll(extraTokens);
        allTokens.removeAll(redundantTokens);
        allTokens.forEach(type -> { tokenClassesByName.put(type.getName(), type); tokenClassesBySimpleName.put(type.getSimpleName(), type); });
        if (tokenClassesByName.size() != tokenClassesBySimpleName.size()) {
            throw new SecurityException(ERR_DUPLICATE_SECURITY_TOKENS);
        }
    }

    /**
     * Additional initialisation after the constructor.
     * Called by the IoC framework.
     */
    @Inject
    private void init(
            final AuditingMode auditingMode,
            final IApplicationDomainProvider appDomain,
            final IAuditTypeFinder auditTypeFinder,
            final ISecurityTokenGenerator generator,
            final @Named("tokens.package") String tokensPkgName)
    {
        if (auditingMode == AuditingMode.ENABLED) {
            registerAuditTokens(appDomain, auditTypeFinder, generator, tokensPkgName);
        }

        if (tokenClassesByName.size() != tokenClassesBySimpleName.size()) {
            throw new SecurityException(ERR_DUPLICATE_SECURITY_TOKENS);
        }
        topLevelSecurityTokenNodes = buildTokenNodes(tokenClassesByName.values());
    }

    private void registerAuditTokens(
            final IApplicationDomainProvider appDomain,
            final IAuditTypeFinder auditTypeFinder,
            final ISecurityTokenGenerator generator,
            final String tokensPkgName)
    {
        final var auditTokensPkgName = tokensPkgName + ".audit";

        appDomain.entityTypes().stream()
                .filter(AuditUtils::isAudited)
                .flatMap(ty -> {
                    final var synAuditEntityType = auditTypeFinder.navigate(ty).synAuditEntityType();
                    return templatesForAuditedType(ty)
                            .stream()
                            .map(template -> generator.generateToken(synAuditEntityType,
                                                                     template,
                                                                     Optional.of(auditTokensPkgName),
                                                                     Optional.of(AuditModuleToken.class)));
                })
                .forEach(tok -> {
                    tokenClassesByName.put(tok.getName(), tok);
                    tokenClassesBySimpleName.put(tok.getSimpleName(), tok);
                });
    }

    /**
     * Given an audited entity type, specifies the kinds of tokens that should be generated.
     */
    protected Set<Template> templatesForAuditedType(final Class<? extends AbstractEntity<?>> type) {
        return Set.of(Template.READ, Template.READ_MODEL);
    }

    @Override
    public SortedSet<SecurityTokenNode> getTopLevelSecurityTokenNodes() {
        return Collections.unmodifiableSortedSet(topLevelSecurityTokenNodes);
    }

    @Override
    public Collection<Class<? extends ISecurityToken>> allSecurityTokens() {
        return unmodifiableCollection(tokenClassesByName.values());
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

    /// Transforms a set of security tokens into a hierarchy of [SecurityTokenNode] nodes.
    ///
    /// The result is a forest of trees (i.e., multiple trees), ordered according to the comparator, implemented by [SecurityTokenNode].
    /// Roots for each trees represent one of the top-level security tokens.
    ///
    /// `allTokens` must contain all tokens that are contained in the resulting forest of trees.
    /// For example, it is an error if `allTokens` contains a sub-token but does not contain its parent token.
    private static SortedSet<SecurityTokenNode> buildTokenNodes(final Iterable<Class<? extends ISecurityToken>> allTokens) {
        final Map<Class<? extends ISecurityToken>, SecurityTokenNode> tokenTypeToNode = new HashMap<>(Iterables.size(allTokens));
        allTokens.forEach(t -> buildTokenNodes_(t, tokenTypeToNode));

        if (tokenTypeToNode.size() != Iterables.size(allTokens)) {
            final var unregisteredTokens = disjunction(tokenTypeToNode.keySet(), allTokens);
            throw new SecurityException(format(
                    "There are %s unregistered tokens. They should be registered with [%s]. Unregistered tokens: [%s]",
                    unregisteredTokens.size(),
                    ISecurityTokenProvider.class.getSimpleName(),
                    CollectionUtil.toString(unregisteredTokens, Class::getSimpleName, ", ")));
        }

        return tokenTypeToNode.values()
                .stream()
                .filter(node -> node.getSuperTokenNode() == null)
                .collect(toCollection(TreeSet::new));
    }

    /// Builds a token node for `tokenType`.
    /// Mutates `tokenTypeToNode` in the process.
    private static SecurityTokenNode buildTokenNodes_(
            final Class<? extends ISecurityToken> tokenType,
            final Map<Class<? extends ISecurityToken>, SecurityTokenNode> tokenTypeToNode)
    {
        final var existingTokenNode = tokenTypeToNode.get(tokenType);
        if (existingTokenNode != null) {
            return existingTokenNode;
        }
        else {
            final SecurityTokenNode tokenNode;
            final var superclass = tokenType.getSuperclass();
            // Sub-token
            if (ISecurityToken.class.isAssignableFrom(superclass)) {
                final var superTokenNode = buildTokenNodes_((Class) superclass, tokenTypeToNode);
                tokenNode = new SecurityTokenNode(tokenType, superTokenNode);
            }
            // Top-level token
            else {
                tokenNode = new SecurityTokenNode(tokenType, null);
            }
            tokenTypeToNode.put(tokenType, tokenNode);
            return tokenNode;
        }
    }

}
