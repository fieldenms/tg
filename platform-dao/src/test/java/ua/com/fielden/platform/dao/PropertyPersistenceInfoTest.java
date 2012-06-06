package ua.com.fielden.platform.dao;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import static org.junit.Assert.assertEquals;

public class PropertyPersistenceInfoTest extends BaseEntQueryTCase {

    @Test
    public void test() {
	final PropertyPersistenceInfo bogieLocationProp = new PropertyPersistenceInfo.Builder("location", TgBogieLocation.class, true). //
		column(new PropertyColumn("location")). //
		type(PropertyPersistenceType.UNION_ENTITY). //
		build();
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("location.wagonSlot", WAGON_SLOT, true, hibType("long"), "LOCATION_WAGONSLOT", PropertyPersistenceType.UNION_DETAILS));
	expected.add(ppi("location.workshop", WORKSHOP, true, hibType("long"), "LOCATION_WORKSHOP", PropertyPersistenceType.UNION_DETAILS));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(bogieLocationProp.getComponentTypeSubprops());
	assertEquals("Incorrect result type", expected, actual);
    }
}