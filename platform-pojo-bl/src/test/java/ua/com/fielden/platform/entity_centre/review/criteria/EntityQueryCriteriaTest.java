package ua.com.fielden.platform.entity_centre.review.criteria;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.domaintree.testing.*;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.interception.AuthenticationTestIocModule;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria.createFetchModelFrom;

/**
 * A test for {@link EntityQueryCriteria}.
 *
 * @author TG Team
 *
 */
public class EntityQueryCriteriaTest {

    private final static Injector injector = new ApplicationInjectorFactory()
            .add(new AuthenticationTestIocModule())
            .getInjector();

    @Test
    public void absence_of_short_collection_in_root_type_does_not_require_fetching_of_parent_keys() {
        final Set<String> properties = new HashSet<>();
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty(), Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class);
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_in_root_type_ensures_fetching_of_parent_keys() {
        final Set<String> properties = new HashSet<>();
        properties.add("shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty(), Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumentedWithKeyAndDesc(MasterEntity.class).
                with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortSlaveEntity.class));
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_inside_inner_property_ensures_fetching_of_inner_property_keys() {
        final Set<String> properties = new HashSet<>();
        properties.add("entityProp.shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty(), Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class).
                with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(SlaveEntity.class)
                        .with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortEvenSlaverEntity.class)));
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_inside_two_level_inner_property_ensures_fetching_of_inner_property_keys() {
        final Set<String> properties = new HashSet<>();
        properties.add("entityProp.entityProp.shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty(), Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class).
                with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(SlaveEntity.class)
                        .with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(EvenSlaverEntity.class)
                                .with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortEvenSlaverEntity.class))
                        )
                );
        assertEquals(expected, actual);
    }

    @Test
    public void authorised_prop_should_be_fetched() {
        final Set<String> properties = new HashSet<>();
        properties.add("authorisedProp");
        final IFetchProvider<AuthorisationTestEntity> actual = createFetchModelFrom(AuthorisationTestEntity.class, properties, Optional.empty(), Optional.empty());
        final IFetchProvider<AuthorisationTestEntity> expected = EntityUtils.fetchNotInstrumented(AuthorisationTestEntity.class).
                with("authorisedProp");
        assertEquals(expected, actual);
    }

    @Test
    public void unauthorised_prop_should_not_be_fetched() {
        final Set<String> properties = new HashSet<>();
        properties.add("unauthorisedProp");
        final IFetchProvider<AuthorisationTestEntity> actual = createFetchModelFrom(AuthorisationTestEntity.class, properties, Optional.empty(), Optional.empty());
        final IFetchProvider<AuthorisationTestEntity> expected = EntityUtils.fetchNotInstrumented(AuthorisationTestEntity.class);
        assertEquals(expected, actual);
    }
}
