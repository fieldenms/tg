package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_DETAILS;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_HEADER;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.entity.query.metadata.PropertyColumn;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;

public class PropertyPersistenceInfoTest extends BaseEntQueryTCase {

    @Test
    public void test() {
        final PropertyMetadata bogieLocationProp = new PropertyMetadata.Builder("location", TgBogieLocation.class, true, PERSISTED). //
        column(new PropertyColumn("location")). //
        category(UNION_ENTITY_HEADER). //
        build();
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("location.wagonSlot", WAGON_SLOT, true, hibType("long"), "LOCATION_WAGONSLOT", UNION_ENTITY_DETAILS, PERSISTED));
        expected.add(ppi("location.workshop", WORKSHOP, true, hibType("long"), "LOCATION_WORKSHOP", UNION_ENTITY_DETAILS, PERSISTED));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(bogieLocationProp.getComponentTypeSubprops());
        assertEquals("Incorrect result type", expected, actual);
    }
}