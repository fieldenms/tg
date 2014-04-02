package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TypeFilter.EntityHasPropertyOfType;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;

/**
 * Test case for {@link TypeFilter}.
 *
 * @author TG Team
 *
 */
public class TypeFilterTest {

    @Test
    public void should_not_filter_out_simple_entity() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SimpleEntity.class);

        final List<Class<? extends AbstractEntity<?>>> result = TypeFilter.filter(types, new EntityHasPropertyOfType(String.class));
        assertTrue(result.contains(SimpleEntity.class));
    }

    @Test
    public void should_not_filter_out_second_level_entity() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SecondLevelEntity.class);

        final List<Class<? extends AbstractEntity<?>>> result = TypeFilter.filter(types, new EntityHasPropertyOfType(SecondLevelEntity.class));
        assertTrue(result.contains(SecondLevelEntity.class));
    }

    @Test
    public void filtering_should_take_into_account_type_hierarchy() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SecondLevelEntity.class);

        final List<Class<? extends AbstractEntity<?>>> result = TypeFilter.filter(types, new EntityHasPropertyOfType(String.class));
        assertTrue(result.contains(SecondLevelEntity.class));
    }

    @Test
    public void filtering_of_multiple_types_with_matching_condition_should_work() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SimpleEntity.class);
        types.add(SecondLevelEntity.class);

        final List<Class<? extends AbstractEntity<?>>> result = TypeFilter.filter(types, new EntityHasPropertyOfType(String.class));

        assertTrue(result.contains(SecondLevelEntity.class));
        assertTrue(result.contains(SimpleEntity.class));
    }

    @Test
    public void filtering_of_multiple_type_with_mixed_condition_should_work() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SimpleEntity.class);
        types.add(FirstLevelEntity.class);
        types.add(SecondLevelEntity.class);

        final List<Class<? extends AbstractEntity<?>>> result = TypeFilter.filter(types, new EntityHasPropertyOfType(SecondLevelEntity.class));

        assertTrue(result.contains(SecondLevelEntity.class));
        assertFalse(result.contains(SimpleEntity.class));
        assertFalse(result.contains(FirstLevelEntity.class));
    }

}
