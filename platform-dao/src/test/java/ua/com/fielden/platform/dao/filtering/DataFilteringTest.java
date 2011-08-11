package ua.com.fielden.platform.dao.filtering;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.WorkOrder;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkorderDao;

import static ua.com.fielden.platform.equery.equery.select;

/**
 * This test case ensures correct implementation of the common DAO functionality in conjunction with Session injection by means of method intercepter.
 *
 * @author TG Team
 *
 */
public class DataFilteringTest extends DbDrivenTestCase {
    private final IWorkorderDao daoWorkOrder = injector.getInstance(IWorkorderDao.class);


    @Test
    public void test_data_filter_for_unordered_simple_query() {
	daoWorkOrder.setUsername("WS1");
	final List<WorkOrder> workOrders = daoWorkOrder.getEntities(select(WorkOrder.class).model(WorkOrder.class), null);

	assertEquals("Incorrect number of work orders.", 4, workOrders.size());
    }

    @Test
    public void test_data_filter_for_unordered_with_where_query() {
	daoWorkOrder.setUsername("WS2");
	final List<WorkOrder> workOrders = daoWorkOrder.getEntities(select(WorkOrder.class).where().prop("equipment.key").eq().val("WAGON1").model(WorkOrder.class), null);

	assertEquals("Incorrect number of work orders.", 1, workOrders.size());
    }

    @Test
    public void test_data_filter_for_ordered_simple_query() {
	daoWorkOrder.setUsername("WS2");
	final List<WorkOrder> workOrders = daoWorkOrder.getEntities(select(WorkOrder.class).orderBy("key desc").model(WorkOrder.class), null);

	assertEquals("Incorrect number of work orders.", 6, workOrders.size());
	assertEquals("Incorrect order of work orders.", "WO_009", workOrders.get(0).getKey());
	assertEquals("Incorrect order of work orders.", "WO_008", workOrders.get(1).getKey());
	assertEquals("Incorrect order of work orders.", "WO_006", workOrders.get(2).getKey());
	assertEquals("Incorrect order of work orders.", "WO_005", workOrders.get(3).getKey());
	assertEquals("Incorrect order of work orders.", "WO_003", workOrders.get(4).getKey());
	assertEquals("Incorrect order of work orders.", "WO_001", workOrders.get(5).getKey());
    }

    @Test
    public void test_data_filter_for_ordered_with_where_query() {
	daoWorkOrder.setUsername("WS2");
	final List<WorkOrder> workOrders = daoWorkOrder.getEntities(select(WorkOrder.class).where().prop("equipment.key").eq().val("WAGON2").orderBy("key desc").model(WorkOrder.class), null);

	assertEquals("Incorrect number of work orders.", 2, workOrders.size());
	assertEquals("Incorrect order of work orders.", "WO_008", workOrders.get(0).getKey());
	assertEquals("Incorrect order of work orders.", "WO_005", workOrders.get(1).getKey());
    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/data-filtering-test-case.flat.xml" };
    }

}
