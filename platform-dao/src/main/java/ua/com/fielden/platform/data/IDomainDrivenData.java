package ua.com.fielden.platform.data;

import java.util.Date;
import java.util.SortedSet;

import org.joda.time.DateTime;

import ua.com.fielden.platform.algorithm.search.ISearchAlgorithm;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

public interface IDomainDrivenData {

    <T extends AbstractEntity<?>> T save(final T instance);

    <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass);

    <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key);

    <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key, final String desc);

    <T extends AbstractEntity<DynamicEntityKey>> T new_composite(final Class<T> entityClass, final Object... keys);

    <T> T getInstance(final Class<T> type);

    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T co(final Class<E> type);

    Date date(final String dateTime);

    DateTime dateTime(final String dateTime);

    default void setupUser(final User.system_users defaultUser, final String emailDomain) {
        // VIRTUAL_USER is a virtual user (cannot be persisted) and has full access to all security tokens
        // It should always be used as the current user for data population activities
        final IUser coUser = co(User.class);
        final User u = new_(User.class, User.system_users.VIRTUAL_USER.name()).setBase(true);
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUser(u);

        final User _su = coUser.save(new_(User.class, defaultUser.name()).setBase(true).setEmail(defaultUser + "@" + emailDomain).setActive(true));
        final User su = coUser.resetPasswd(_su, _su.getKey());

        final UserRole admin = save(new_(UserRole.class, "ADMIN", "A role, which has a full access to the the system and should be used only for users who need administrative previligies.").setActive(true));

        save(new_composite(UserAndRoleAssociation.class, su, admin));
        try {
            final IApplicationSettings settings = getInstance(IApplicationSettings.class);
            final SecurityTokenProvider provider = new SecurityTokenProvider(settings.pathToSecurityTokens(), settings.securityTokensPackageName());
            final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
            final SecurityTokenAssociator predicate = new SecurityTokenAssociator(admin, co(SecurityRoleAssociation.class));
            final ISearchAlgorithm<Class<? extends ISecurityToken>, SecurityTokenNode> alg = new BreadthFirstSearch<Class<? extends ISecurityToken>, SecurityTokenNode>();
            for (final SecurityTokenNode securityNode : topNodes) {
                alg.search(securityNode, predicate);
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        up.setUser(su);
    }

}
