package ua.com.fielden.platform.entity_centre.review.criteria;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.domaintree.testing.*;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.interception.AuthenticationTestIocModule;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Set;

import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria.createFetchModelFrom;

/// A test case for [EntityQueryCriteria].
///
public class EntityQueryCriteriaTest {

    private final static Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .add(new AuthenticationTestIocModule())
            .getInjector();

    @Test
    public void absence_of_short_collection_in_root_type_does_not_require_fetching_of_parent_keys() {
        final var properties = Set.<String>of();
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, empty(), empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class);
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_in_root_type_ensures_fetching_of_parent_keys() {
        final var properties = Set.of("shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, empty(), empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumentedWithKeyAndDesc(MasterEntity.class).
                with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortSlaveEntity.class));
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_inside_inner_property_ensures_fetching_of_inner_property_keys() {
        final var properties = Set.of("entityProp.shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, empty(), empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class).
                with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(SlaveEntity.class)
                        .with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortEvenSlaverEntity.class)));
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_inside_two_level_inner_property_ensures_fetching_of_inner_property_keys() {
        final var properties = Set.of("entityProp.entityProp.shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, empty(), empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class).
                with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(SlaveEntity.class)
                        .with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(EvenSlaverEntity.class)
                                .with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortEvenSlaverEntity.class))
                        )
                );
        assertEquals(expected, actual);
    }

    @Test
    public void authorised_prop_are_fetched() {
        final var properties = Set.of("authorisedProp");
        final var actual = createFetchModelFrom(AuthorisationTestEntity.class, properties, empty(), empty());
        final var expected = EntityUtils.fetchNotInstrumented(AuthorisationTestEntity.class).with("authorisedProp");
        assertEquals(expected, actual);
    }

    @Test
    public void unauthorised_prop_are_not_fetched() {
        final var properties = Set.of("unauthorisedProp");
        final IFetchProvider<AuthorisationTestEntity> actual = createFetchModelFrom(AuthorisationTestEntity.class, properties, empty(), empty());
        final IFetchProvider<AuthorisationTestEntity> expected = EntityUtils.fetchNotInstrumented(AuthorisationTestEntity.class);
        assertEquals(expected, actual);
    }

}
