package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.WorkOrder;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkorderDao;

/**
 * Tests Hibernate type {@link PropertyDescriptorType}.
 *
 * @author TG Team
 */
public class PropertyDescriptorTypeTestCase extends DbDrivenTestCase {
    private final IWorkorderDao dao = injector.getInstance(IWorkorderDao.class);

    public void test_property_descriptor_is_restored_correctly() {
	final WorkOrder wo = dao.findByKey("WO_001");
	assertNotNull("Important property should not be null.", wo.getImportantProperty());
	assertEquals("Incorrect important property.", new PropertyDescriptor<WorkOrder>(WorkOrder.class, "equipment"), wo.getImportantProperty());
    }

    public void test_property_descriptor_is_stored_correctly() {
	final WorkOrder wo = dao.findByKey("WO_003");
	assertNull("Important property should be null.", wo.getImportantProperty());

	final PropertyDescriptor<WorkOrder> pd = new PropertyDescriptor<WorkOrder>(WorkOrder.class, "status");
	wo.setImportantProperty(pd);
	dao.save(wo);

	hibernateUtil.getSessionFactory().getCurrentSession().close();

	assertEquals("Important property was not saved correctly.", pd, dao.findByKey("WO_003").getImportantProperty());
    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/property-descriptor-test-case.flat.xml" };
    }

}