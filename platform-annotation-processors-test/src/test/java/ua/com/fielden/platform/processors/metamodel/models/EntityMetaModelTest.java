package ua.com.fielden.platform.processors.metamodel.models;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Unit test for {@link EntityMetaModel}.
 */
public class EntityMetaModelTest {

    @Test
    public void constructor_does_not_accept_null() {
        final Exception thrown = assertThrows(EntityMetaModelException.class, () -> new MockMetaModel(null));
        assertEquals("%s constructor received null as an argument.".formatted(MockMetaModel.class.getSimpleName()), thrown.getMessage());
    }

    @Test
    public void toPath_returns_this_in_absence_of_context() {
        assertEquals("this", (new MockMetaModel()).toPath());
    }

    /**
     * A helper mock class for testing basic properties of {@link EntityMetaModel}.
     *
     */
    private static class MockMetaModel extends EntityMetaModel {
        public MockMetaModel(final String path) {
            super(path);
        }
        public MockMetaModel() {
            super();
        }

        // IGNORE THIS
        // abstract method implementation
        @Override
        public Class<? extends AbstractEntity> getEntityClass() {
            return null;
        }
    }

}
