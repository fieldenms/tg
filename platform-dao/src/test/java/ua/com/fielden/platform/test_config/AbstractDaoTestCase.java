package ua.com.fielden.platform.test_config;

import java.util.List;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
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
        save(new_(TgPerson.class, "Person who is a user").setUsername("TEST").setBase(true));

        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUser.class));
    }
}
