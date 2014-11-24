package ua.com.fielden.platform.persistence.types;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.ITgWorkorder;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test.DbDrivenTestCase;

/**
 * Tests Hibernate type {@link PropertyDescriptorType}.
 * 
 * @author TG Team
 */
public class PropertyDescriptorTypeTestCase extends DbDrivenTestCase {
    private final ITgWorkorder dao = injector.getInstance(ITgWorkorder.class);

    public void test_property_descriptor_is_restored_correctly() {
        final TgWorkOrder wo = dao.findByKeyAndFetch(fetch(TgWorkOrder.class).with("importantProperty").with("vehicle"), "WO_001");
        assertNotNull("Important property should not be null.", wo.getImportantProperty());
        assertEquals("Incorrect important property.", new PropertyDescriptor<TgWorkOrder>(TgWorkOrder.class, "vehicle"), wo.getImportantProperty());
    }

    public void test_property_descriptor_is_stored_correctly() {
        final TgWorkOrder wo = dao.findByKeyAndFetch(fetch(TgWorkOrder.class).with("importantProperty").with("vehicle"), "WO_002");
        assertNull("Important property should be null.", wo.getImportantProperty());

        final PropertyDescriptor<TgWorkOrder> pd = new PropertyDescriptor<TgWorkOrder>(TgWorkOrder.class, "vehicle");
        wo.setImportantProperty(pd);
        dao.save(wo);

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        assertEquals("Important property was not saved correctly.", pd, dao.findByKeyAndFetch(fetch(TgWorkOrder.class).with("importantProperty").with("vehicle"), "WO_002").getImportantProperty());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
        return new String[] { "src/test/resources/data-files/property-descriptor-test-case2.flat.xml" };
    }

}