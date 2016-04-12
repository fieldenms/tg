package ua.com.fielden.platform.test_config;

import java.util.List;
import java.util.SortedSet;

import org.joda.time.DateTime;

import ua.com.fielden.platform.algorithm.search.ISearchAlgorithm;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * Should be used as a convenient base class for domain driven test cases.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDaoTestCase extends AbstractDomainDrivenTestCase {

    public static final String UNIT_TEST_USER = User.system_users.UNIT_TEST_USER.name();
    public static final String UNIT_TEST_ROLE = "UNIT_TEST_ROLE";
    
    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

    /**
     * Initialises a test user. Needs to be invoked in descendant classes.
     */
    @Override
    protected void populateDomain() {
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(new DateTime());

        // some tests require current person, thus, need to persist a person who would be a user at the same time
        final IUser coUser = ao(User.class);
        final User u = new_(User.class, UNIT_TEST_USER).setBase(true);
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUser(u);
        final User testUser = coUser.save(u);
        save(new_(TgPerson.class, "Person who is a user").setUser(testUser));

        // add a test user role
        final UserRole admin = save(new_(UserRole.class, UNIT_TEST_ROLE, "Test role with access to all security tokens."));
        // associate the test role with the test user
        save(new_composite(UserAndRoleAssociation.class, testUser, admin));
        
        // provide access to all security tokens for the test role
        final IApplicationSettings settings = config.getInstance(IApplicationSettings.class);
        final SecurityTokenProvider provider = new SecurityTokenProvider(settings.pathToSecurityTokens(), settings.securityTokensPackageName()); //  IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.path"), IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.package")
        final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
        final SecurityTokenAssociator predicate = new SecurityTokenAssociator(admin, ao(SecurityRoleAssociation.class));
        final ISearchAlgorithm<Class<? extends ISecurityToken>, SecurityTokenNode> alg = new BreadthFirstSearch<Class<? extends ISecurityToken>, SecurityTokenNode>();
        for (final SecurityTokenNode securityNode : topNodes) {
            alg.search(securityNode, predicate);
        }
        
        up.setUsername(testUser.getKey(), getInstance(IUser.class));
    }
}
