package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;
import static ua.com.fielden.platform.web.centre.api.resultset.PropDef.mkProp;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;

/**
 * A test case for Entity Centre DSL produced result sets.
 *
 * @author TG Team
 *
 */
public class EntityCentreBuilderResultSetTest {

    @Test
    public void order_of_properties_and_custom_properties_should_be_the_same_as_during_centre_definition() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key")
                .also()
                .addProp(mkProp("OF", "Defect OFF road", "OF"))
                .also()
                .addProp(mkProp("ON", "Defect ON road", "ON"))
                .also()
                .addProp("desc")
                .build();

        assertFalse(config.getSelectionCriteria().isPresent());
        assertTrue(config.getResultSetProperties().isPresent());
        assertEquals(4, config.getResultSetProperties().get().size());

        assertTrue(config.getResultSetProperties().get().get(0).propName.isPresent());
        assertFalse(config.getResultSetProperties().get().get(0).propDef.isPresent());
        assertEquals("key", config.getResultSetProperties().get().get(0).propName.get());

        assertFalse(config.getResultSetProperties().get().get(1).propName.isPresent());
        assertTrue(config.getResultSetProperties().get().get(1).propDef.isPresent());
        assertEquals("OF", config.getResultSetProperties().get().get(1).propDef.get().title);

        assertFalse(config.getResultSetProperties().get().get(2).propName.isPresent());
        assertTrue(config.getResultSetProperties().get().get(2).propDef.isPresent());
        assertEquals("ON", config.getResultSetProperties().get().get(2).propDef.get().title);

        assertTrue(config.getResultSetProperties().get().get(3).propName.isPresent());
        assertFalse(config.getResultSetProperties().get().get(3).propDef.isPresent());
        assertEquals("desc", config.getResultSetProperties().get().get(3).propName.get());
    }

}
