package ua.com.fielden.platform.dao;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import static org.junit.Assert.assertEquals;

public class PropertyPersistenceInfoTest extends BaseEntQueryTCase {

    @Test
    public void test() {
	final PropertyMetadata bogieLocationProp = new PropertyMetadata.Builder("location", TgBogieLocation.class, true). //
		column(new PropertyColumn("location")). //
		type(PropertyCategory.UNION_ENTITY_HEADER). //
		build();
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("location.wagonSlot", WAGON_SLOT, true, hibType("long"), "LOCATION_WAGONSLOT", PropertyCategory.UNION_ENTITY_DETAILS));
	expected.add(ppi("location.workshop", WORKSHOP, true, hibType("long"), "LOCATION_WORKSHOP", PropertyCategory.UNION_ENTITY_DETAILS));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(bogieLocationProp.getComponentTypeSubprops());
	assertEquals("Incorrect result type", expected, actual);
    }
}