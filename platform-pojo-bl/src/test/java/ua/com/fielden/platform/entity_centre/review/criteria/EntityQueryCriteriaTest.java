package ua.com.fielden.platform.entity_centre.review.criteria;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria.createFetchModelFrom;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.ShortEvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.ShortSlaveEntity;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A test for {@link EntityQueryCriteria}.
 *
 * @author TG Team
 *
 */
public class EntityQueryCriteriaTest {

    @Test
    public void absence_of_short_collection_in_root_type_does_not_require_fetching_of_parent_keys() {
        final Set<String> properties = new HashSet<>();
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class);
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_in_root_type_ensures_fetching_of_parent_keys() {
        final Set<String> properties = new HashSet<>();
        properties.add("shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumentedWithKeyAndDesc(MasterEntity.class).
                with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortSlaveEntity.class));
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_inside_inner_property_ensures_fetching_of_inner_property_keys() {
        final Set<String> properties = new HashSet<>();
        properties.add("entityProp.shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class).
                with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(SlaveEntity.class)
                        .with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortEvenSlaverEntity.class)));
        assertEquals(expected, actual);
    }

    @Test
    public void short_collection_inside_two_level_inner_property_ensures_fetching_of_inner_property_keys() {
        final Set<String> properties = new HashSet<>();
        properties.add("entityProp.entityProp.shortCollection");
        final IFetchProvider<MasterEntity> actual = createFetchModelFrom(MasterEntity.class, properties, Optional.empty());
        final IFetchProvider<MasterEntity> expected = EntityUtils.fetchNotInstrumented(MasterEntity.class).
                with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(SlaveEntity.class)
                        .with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(EvenSlaverEntity.class)
                                .with("shortCollection", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(ShortEvenSlaverEntity.class))
                        )
                );
        assertEquals(expected, actual);
    }
}
