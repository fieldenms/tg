package ua.com.fielden.platform.processors.metamodel.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelException;

/**
 * Unit test for {@link EntityMetaModel}.
 */
public class EntityMetaModelTest {

    private class MockMetaModel extends EntityMetaModel {
        public MockMetaModel(String path) {
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

    @Test
    public void constructor_doesnt_accept_null() {
        Exception thrown = assertThrows(EntityMetaModelException.class, () -> new MockMetaModel(null));
        assertEquals(String.format("%s constructor received null as an argument.", MockMetaModel.class.getSimpleName()), thrown.getMessage());
    }

    @Test
    public void toPath_returns_this_in_absence_of_context() {
        assertEquals("this", (new MockMetaModel()).toPath());
    }
}
