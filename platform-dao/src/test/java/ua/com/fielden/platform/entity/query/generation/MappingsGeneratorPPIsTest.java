package ua.com.fielden.platform.entity.query.generation;

import java.util.SortedSet;

import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyPersistenceInfo;

public class MappingsGeneratorPPIsTest extends BaseEntQueryTCase {

    @Test
    public void test1() {
	final SortedSet<PropertyPersistenceInfo> ppis = MAPPINGS_GENERATOR.getEntityPPIs(VEHICLE);
	for (final PropertyPersistenceInfo propertyPersistenceInfo : ppis) {
	    System.out.println("++++++++++++++++++++++++++++++++++++++");
	    System.out.println(propertyPersistenceInfo);
	    System.out.println("++++++++++++++++++++++++++++++++++++++");
	}

	//assertEquals("Incorrect result type", EntityAggregates.class, entResultQry(qry).getResultType());
    }
}
