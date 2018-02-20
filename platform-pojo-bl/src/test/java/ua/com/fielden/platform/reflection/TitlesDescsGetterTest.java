package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * Test case for {@link TitlesDescsGetter}.
 * 
 * @author TG Team
 * 
 */
public class TitlesDescsGetterTest {

    @KeyTitle(value = "Wo No", desc = "Work order number")
    @DescTitle(value = "Description", desc = "Work order description")
    @EntityTitle(value = "Work Order", desc = "Domain entity representing an order for a maintenance of equipment.")
    @KeyType(String.class)
    private static class C extends AbstractEntity<String> {
        @IsProperty
        @Title(value = "Veh/Eqp", desc = "Vehicle or other equipment")
        private Integer vehicle;
        @IsProperty
        @Title(value = "Incident", desc = "Incident with which this work order is associated.")
        private Integer incident;
        @IsProperty
        @Title(value = "This typed property", desc = "This typed property description")
        private C c;
    }

    @KeyType(String.class)
    private static class EntityWithoutTitle extends AbstractEntity<String> {
    }

    @Test
    public void test_property_titles_and_descs_determination() {
        assertEquals("Should be equal.", new Pair<String, String>("Wo No", "Work order number"), TitlesDescsGetter.getTitleAndDesc("key", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Description", "Work order description"), TitlesDescsGetter.getTitleAndDesc("desc", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Veh/Eqp", "Vehicle or other equipment"), TitlesDescsGetter.getTitleAndDesc("vehicle", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Incident", "Incident with which this work order is associated."), TitlesDescsGetter.getTitleAndDesc("incident", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Veh/Eqp", "Vehicle or other equipment"), TitlesDescsGetter.getTitleAndDesc("c.c.vehicle", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Incident", "Incident with which this work order is associated."), TitlesDescsGetter.getTitleAndDesc("c.c.c.c.c.c.incident", C.class));
    }

    @Test
    public void test_entity_titles_and_descs_determination() {
        assertEquals("Should be equal.", new Pair<String, String>("Work Order", "Domain entity representing an order for a maintenance of equipment."), TitlesDescsGetter.getEntityTitleAndDesc(C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Entity Without Title", "Entity Without Title entity"), TitlesDescsGetter.getEntityTitleAndDesc(EntityWithoutTitle.class));
    }

    @Test
    public void test_full_property_titles_and_descs_determination() {
        assertEquals("Should be equal.", new Pair<String, String>("Wo No", "<html><i><b>Work order number</b></i><br><i>[Wo No]</i></html>"), TitlesDescsGetter.getFullTitleAndDesc("key", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Description", "<html><i><b>Work order description</b></i><br><i>[Description]</i></html>"), TitlesDescsGetter.getFullTitleAndDesc("desc", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Veh/Eqp", "<html><i><b>Vehicle or other equipment</b></i><br><i>[Veh/Eqp]</i></html>"), TitlesDescsGetter.getFullTitleAndDesc("vehicle", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Incident", "<html><i><b>Incident with which this work order is associated.</b></i><br><i>[Incident]</i></html>"), TitlesDescsGetter.getFullTitleAndDesc("incident", C.class));
        final String add = TitlesDescsGetter.LEFT_ARROW + "This typed property";
        assertEquals("Should be equal.", new Pair<String, String>("Veh/Eqp" + add + add, "<html><i><b>Vehicle or other equipment</b></i><br><i>[Veh/Eqp" + add + add
                + "]</i></html>"), TitlesDescsGetter.getFullTitleAndDesc("c.c.vehicle", C.class));
        assertEquals("Should be equal.", new Pair<String, String>("Incident" + add + add + add + add + add + add, "<html><i><b>Incident with which this work order is associated.</b></i><br><i>[Incident"
                + add + add + add + add + add + add + "]</i></html>"), TitlesDescsGetter.getFullTitleAndDesc("c.c.c.c.c.c.incident", C.class));
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_title_and_desc_of_entity_type_by_property_path() {
        final Optional<Pair<String, String>> titleAndDesc = TitlesDescsGetter.getTitleAndDescOfPropertyType("critOnlyAEProperty", FirstLevelEntity.class);
        
        assertTrue(titleAndDesc.isPresent());
        assertEquals("Simple Entity", titleAndDesc.get().getKey());
        assertEquals("Simple Entity entity", titleAndDesc.get().getValue());
    }
    
    @Test
    public void getTitleAndDescOfPropertyType_returns_empty_result_if_non_entity_typed_property_is_specified() {
        final Optional<Pair<String, String>> titleAndDesc = TitlesDescsGetter.getTitleAndDescOfPropertyType("property", FirstLevelEntity.class);
        
        assertFalse(titleAndDesc.isPresent());
    }
}
