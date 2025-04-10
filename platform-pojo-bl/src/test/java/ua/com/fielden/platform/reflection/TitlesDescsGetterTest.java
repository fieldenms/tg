package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
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
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntityPathDependentTitles;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;
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
    public void getTitleAndDesc_uses_entity_type_title_for_entity_typed_properties_that_miss_annotation_Title_as_their_title_and_desc() {
        final Pair<String, String> propTitleAndDesc = TitlesDescsGetter.getTitleAndDesc("critOnlyAEProperty", FirstLevelEntity.class);
        final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(SimpleEntity.class);

        assertEquals(entityTitleAndDesc.getKey(), propTitleAndDesc.getKey());
        assertEquals(entityTitleAndDesc.getKey(), propTitleAndDesc.getValue());
    }

    @Test
    public void getTitleAndDesc_can_determine_prop_title_and_desc_from_IConvertableToPath() {
        final Pair<String, String> baseCaseTitleAndDesc = TitlesDescsGetter.getTitleAndDesc("prop2.propertyTwo", FirstLevelEntityPathDependentTitles.class);
        assertEquals("Two", baseCaseTitleAndDesc.getKey());
        assertEquals("Two", baseCaseTitleAndDesc.getValue());
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_title_and_desc_of_entity_type_by_property_path() {
        final Optional<Pair<String, String>> titleAndDesc = TitlesDescsGetter.getTitleAndDescOfPropertyType("critOnlyAEProperty", FirstLevelEntity.class);

        assertTrue(titleAndDesc.isPresent());
        assertEquals("Simple Entity", titleAndDesc.get().getKey());
        assertEquals("Simple Entity entity", titleAndDesc.get().getValue());
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_title_and_desc_of_non_entity_typed_property() {
        final Optional<Pair<String, String>> titleAndDesc = TitlesDescsGetter.getTitleAndDescOfPropertyType("property", FirstLevelEntity.class);

        assertTrue(titleAndDesc.isPresent());
        assertEquals("Property", titleAndDesc.get().getKey());
        assertEquals("Property", titleAndDesc.get().getValue());
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_path_dependent_title_and_desc_for_one_level_deep_property_path() {
        final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc("prop1.property", FirstLevelEntityPathDependentTitles.class);

        assertEquals("Nested title", titleAndDesc.getKey());
        assertEquals("Nested desc", titleAndDesc.getValue());
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_path_dependent_title_and_desc_for_one_level_deep_property_path_referencing_key() {
        final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc("prop1.key", FirstLevelEntityPathDependentTitles.class);

        assertEquals("New Key Title", titleAndDesc.getKey());
        assertEquals("New Key desc", titleAndDesc.getValue());
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_path_dependent_title_and_desc_for_one_level_deep_property_path_referencing_desc() {
        final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc("prop1.desc", FirstLevelEntityPathDependentTitles.class);

        assertEquals("New Desc Title", titleAndDesc.getKey());
        assertEquals("New Desc desc", titleAndDesc.getValue());
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_path_dependent_title_and_desc_for_one_and_two_level_deep_property_paths_defined_for_the_same_root_prop() {
        final Pair<String, String> level1TitleAndDesc = TitlesDescsGetter.getTitleAndDesc("prop2.critOnlyAEProperty", FirstLevelEntityPathDependentTitles.class);

        assertEquals("First Level Nested Title", level1TitleAndDesc.getKey());
        assertEquals("First Level Nested Desc", level1TitleAndDesc.getValue());

        final Pair<String, String> level2TitleAndDesc = TitlesDescsGetter.getTitleAndDesc("prop2.critOnlyAEProperty.propertyTwo", FirstLevelEntityPathDependentTitles.class);

        assertEquals("Second Level Nested Title", level2TitleAndDesc.getKey());
        assertEquals("Second Level Nested Desc", level2TitleAndDesc.getValue());
    }

    @Test
    public void getTitleAndDescOfPropertyType_can_determine_path_independent_title_and_desc_in_the_presence_of_annotation_subtitles_for_root_property() {
        final Pair<String, String> baseCaseTitleAndDesc = TitlesDescsGetter.getTitleAndDesc("prop2.propertyTwo", FirstLevelEntityPathDependentTitles.class);
        assertEquals("Two", baseCaseTitleAndDesc.getKey());
        assertEquals("Two", baseCaseTitleAndDesc.getValue());
    }

    @Test
    public void breakClassName_handles_empty_strings_as_empty() {
        assertEquals("", TitlesDescsGetter.breakClassName(""));
        assertEquals("", TitlesDescsGetter.breakClassName(null));
        assertEquals("", TitlesDescsGetter.breakClassName(" "));
    }

    @Test
    public void breakClassName_breaks_strings_by_upper_cased_words() {
        assertEquals("nouppercase", TitlesDescsGetter.breakClassName("nouppercase"));
        assertEquals("one Uppercase", TitlesDescsGetter.breakClassName("oneUppercase"));
        assertEquals("two Upper Cases", TitlesDescsGetter.breakClassName("twoUpperCases"));
        assertEquals("Tree Upper Cases", TitlesDescsGetter.breakClassName("TreeUpperCases"));
        assertEquals("Tree Upper Cases", TitlesDescsGetter.breakClassName("Tree UpperCases"));
        assertEquals("Tree Upper Cases", TitlesDescsGetter.breakClassName("Tree Upper Cases "));
    }

}
