package ua.com.fielden.platform.test_config;

import org.joda.time.DateTime;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.algorithm.search.ISearchAlgorithm;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.security_tokens.TgFuelType_CanSaveNew_Token;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Should be used as a convenient base class for domain driven test cases.
 *
 * @author TG Team
 *
 */
@RunWith(H2OrPostgreSqlOrSqlServerContextSelector.class)
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
        resetIdGenerator();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(new DateTime());

        // VIRTUAL_USER is a virtual user (cannot be persisted) and has full access to all security tokens
        // It should always be used as the current user for data population activities
        final IUser coUser = co$(User.class);
        final User vu = new_(User.class, User.system_users.VIRTUAL_USER.name()).setBase(true).setEmail(User.system_users.VIRTUAL_USER.name() + "@unit-test.software").setActive(true);
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUser(vu);
        
        final User testUser;
        if (useSavedDataPopulationScript()) {
           testUser = coUser.findUser(UNIT_TEST_USER);
        } else {
            // some tests require current person, thus, need to persist a person who would be a user at the same time
            testUser = coUser.save(new_(User.class, UNIT_TEST_USER).setBase(true).setEmail(UNIT_TEST_USER + "@unit-test.software").setActive(true));
            save(new_(TgPerson.class, "Person who is a user").setUser(testUser));
    
            // add a test user role
            final UserRole admin = save(new_(UserRole.class, UNIT_TEST_ROLE, "Test role with access to all security tokens.").setActive(true));
            // associate the test role with the test user
            save(new_composite(UserAndRoleAssociation.class, testUser, admin));

            // provide access to all security tokens for the test role
            final ISecurityTokenProvider provider = getInstance(ISecurityTokenProvider.class);
            final SortedSet<SecurityTokenNode> topNodes = new TreeSet<>();
            topNodes.addAll(provider.getTopLevelSecurityTokenNodes());
            topNodes.add(SecurityTokenNode.makeTopLevelNode(TgFuelType_CanSaveNew_Token.class)); // extra token, which is not loaded by default due to its package
            final SecurityTokenAssociator predicate = new SecurityTokenAssociator(admin, co$(SecurityRoleAssociation.class));
            final ISearchAlgorithm<Class<? extends ISecurityToken>, SecurityTokenNode> alg = new BreadthFirstSearch<Class<? extends ISecurityToken>, SecurityTokenNode>();
            for (final SecurityTokenNode securityNode : topNodes) {
                alg.search(securityNode, predicate);
            }
        }

        up.setUsername(testUser.getKey(), getInstance(IUser.class));
    }

    @Override
    public User getUser() {
        final IUserProvider up = getInstance(IUserProvider.class);
        return up.getUser();
    }

}