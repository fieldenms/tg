package ua.com.fielden.platform.persistence.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.meta.PropertyDescriptor.fromString;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.ITgWorkOrder;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * Tests Hibernate type {@link PropertyDescriptorType}.
 * 
 * @author TG Team
 */
public class PropertyDescriptorTypeTestCase extends AbstractDaoTestCase {
    
    @Test
    public void property_descriptor_is_restored_correctly() {
        final ITgWorkOrder co = co(TgWorkOrder.class);
        
        final TgWorkOrder wo = co.findByKeyAndFetch(fetch(TgWorkOrder.class).with("importantProperty").with("vehicle"), "WO_001");
        assertNotNull("Important property should not be null.", wo.getImportantProperty());
        assertEquals("Incorrect important property.", new PropertyDescriptor<TgWorkOrder>(TgWorkOrder.class, "vehicle"), wo.getImportantProperty());
    }

    @Test
    public void property_descriptor_is_stored_correctly() {
        final ITgWorkOrder co$ = co$(TgWorkOrder.class);
        
        final TgWorkOrder wo = co$.findByKeyAndFetch(fetch(TgWorkOrder.class).with("importantProperty").with("vehicle"), "WO_002");
        assertNull("Important property should be null.", wo.getImportantProperty());

        final PropertyDescriptor<TgWorkOrder> pd = new PropertyDescriptor<TgWorkOrder>(TgWorkOrder.class, "vehicle");
        wo.setImportantProperty(pd);
        co$.save(wo);

        assertEquals("Important property was not saved correctly.", pd, co$.findByKeyAndFetch(fetch(TgWorkOrder.class).with("importantProperty").with("vehicle"), "WO_002").getImportantProperty());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(new_(TgWorkOrder.class, "WO_001", "desc1").setActCost(Money.zero).setEstCost(Money.zero).setYearlyCost(Money.zero).setImportantProperty(fromString("ua.com.fielden.platform.sample.domain.TgWorkOrder:vehicle")));
        save(new_(TgWorkOrder.class, "WO_002", "desc2").setActCost(Money.zero).setEstCost(Money.zero).setYearlyCost(Money.zero));
    }

}