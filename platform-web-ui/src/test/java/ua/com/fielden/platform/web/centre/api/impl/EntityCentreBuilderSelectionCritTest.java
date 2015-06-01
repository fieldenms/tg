package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.multi;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.range;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.single;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.helpers.CustomOrgUnit1Matcher;
import ua.com.fielden.platform.web.centre.api.impl.helpers.CustomVehicleMatcher;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForMultiBoolean;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForMultiString;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForRangeDate;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForRangeDecimal;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForRangeInteger;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForSingleBoolean;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForSingleDate;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForSingleDecimal;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForSingleInteger;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForSingleString;
import ua.com.fielden.platform.web.centre.api.impl.helpers.DefaultValueAssignerForSingleTgOrgUnit;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.*;
import static ua.com.fielden.platform.web.interfaces.ILayout.Orientation.*;


/**
 * A test case for Entity Centre DSL produced selection criteria.
 *
 * @author TG Team
 *
 */
public class EntityCentreBuilderSelectionCritTest {

    @Test
    public void selection_criteria_should_support_custom_autcompleters_with_custom_context() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class).withMatcher(CustomVehicleMatcher.class)
                .also()
                .addCrit("orgUnit1").asMulti().autocompleter(TgOrgUnit1.class).withMatcher(CustomOrgUnit1Matcher.class, context().withCurrentEntity().withSelectionCrit().build())
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals("vehicle", config.getSelectionCriteria().get().get(0));
        assertEquals("orgUnit1", config.getSelectionCriteria().get().get(1));
        assertTrue(config.getValueMatchersForSelectionCriteria().isPresent());
        assertEquals(2, config.getValueMatchersForSelectionCriteria().get().size());
        assertEquals(CustomVehicleMatcher.class, config.getValueMatchersForSelectionCriteria().get().get("vehicle").getKey());
        assertFalse(config.getValueMatchersForSelectionCriteria().get().get("vehicle").getValue().isPresent());
        assertEquals(CustomOrgUnit1Matcher.class, config.getValueMatchersForSelectionCriteria().get().get("orgUnit1").getKey());
        assertTrue(config.getValueMatchersForSelectionCriteria().get().get("orgUnit1").getValue().isPresent());
    }

    @Test
    public void selection_criteria_should_not_be_permit_the_same_property_to_be_added_more_than_once() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class).withMatcher(CustomVehicleMatcher.class)
                    .also()
                    .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][]]")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Provided value '%s' has been already added as a selection criterion for entity '%s'", "vehicle", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void this_keyword_should_not_be_permitted_as_range_selection_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("this").asRange().date()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][]]")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Entity itself (this) cannot be used as a range-valued criterion.", ex.getMessage());
        }
    }

    @Test
    public void this_keyword_should_not_be_permitted_as_single_valued_selection_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("this").asSingle().autocompleter(TgVehicle.class).withMatcher(CustomVehicleMatcher.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][]]")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Entity itself (this) cannot be used as a single-valued criterion.", ex.getMessage());
        }
    }

    @Test
    public void this_keyword_should_not_be_permitted_as_multi_valued_selection_criteria_of_non_matching_autocompleter_type() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("this").asMulti().autocompleter(TgVehicle.class).withMatcher(CustomVehicleMatcher.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][]]")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' has type %s, but type %s has been specified instead.", "this", TgWorkOrder.class.getSimpleName(), TgWorkOrder.class.getSimpleName(), TgVehicle.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void this_keyword_should_be_permitted_as_multi_valued_selection_criteria() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("this").asMulti().autocompleter(TgWorkOrder.class)
                .also()
                .addCrit("orgUnit1").asMulti().autocompleter(TgOrgUnit1.class).withMatcher(CustomOrgUnit1Matcher.class, context().withCurrentEntity().withSelectionCrit().build())
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals("this", config.getSelectionCriteria().get().get(0));
        assertEquals("orgUnit1", config.getSelectionCriteria().get().get(1));
        assertTrue(config.getValueMatchersForSelectionCriteria().isPresent());
        assertEquals(1, config.getValueMatchersForSelectionCriteria().get().size());
        assertEquals(CustomOrgUnit1Matcher.class, config.getValueMatchersForSelectionCriteria().get().get("orgUnit1").getKey());
        assertTrue(config.getValueMatchersForSelectionCriteria().get().get("orgUnit1").getValue().isPresent());
    }

    @Test
    public void selection_criteria_should_not_permit_invalid_property_expressions() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("non.existing.property").asMulti().autocompleter(TgVehicle.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Provided value '%s' is not a valid property expression for entity '%s'", "non.existing.property", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_non_critonly_as_single_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("vehicle").asSingle().autocompleter(TgVehicle.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a single-valued criterion due to missing @CritOnly(SINGLE) in its definition.", "vehicle", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_range_critonly_as_single_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("orgunitCritOnly").asSingle().autocompleter(TgOrgUnit1.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a single-valued criterion due to its definition as @CritOnly(RANGE).", "orgunitCritOnly", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_critonly_as_multi_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("orgunitCritOnlySingle").asMulti().autocompleter(TgOrgUnit1.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a multi-valued criterion due to its definition as @CritOnly(SINGLE).", "orgunitCritOnlySingle", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_critonly_as_range_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("intSingle").asRange().integer()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a range-valued criterion due to its definition as @CritOnly(SINGLE).", "intSingle", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_permit_single_critonly_as_single_valued_criteria() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("intSingle").asSingle().integer()
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals("intSingle", config.getSelectionCriteria().get().get(0));
    }

    @Test
    public void selection_criteria_should_permit_range_critonly_as_range_valued_criteria() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("intRange").asRange().integer()
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals("intRange", config.getSelectionCriteria().get().get(0));
    }

    @Test
    public void selection_criteria_should_not_permit_multi_valued_crit_as_autocompleter_with_null_type() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("orgunitCritOnly").asMulti().autocompleter(null) // trying to trick the system
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Property type is a required argument and cannot be omitted.", ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_multi_valued_crit_as_autocompleter_for_non_entity_type() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("key").asMulti().autocompleter(TgWorkOrder.class) // trying to trick the system
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for autocompletion as it is not of an entity type (%s).", "key", TgWorkOrder.class.getSimpleName(), String.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_multi_valued_crit_as_autocompleter_with_non_matching_property_type() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("orgunitCritOnly").asMulti().autocompleter(TgWorkOrder.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' has type %s, but type %s has been specified instead.", "orgunitCritOnly", TgWorkOrder.class.getSimpleName(), TgOrgUnit1.class.getSimpleName(), TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_multi_valued_crit_as_text_for_non_string_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("vehicle").asMulti().text()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a text component as it is not of type String (%s).", "vehicle", TgWorkOrder.class.getSimpleName(), TgVehicle.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_multi_valued_crit_as_bool_for_non_boolean_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("vehicle.key").asMulti().bool()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a boolean component as it is not of type boolean (%s).", "vehicle.key", TgWorkOrder.class.getSimpleName(), String.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_with_all_possible_multi_valued_criteria_should_be_constructed_successfully() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("vehicle.active").asMulti().bool()
                .also()
                .addCrit("vehicle.desc").asMulti().text()
                .also()
                .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals(3, config.getSelectionCriteria().get().size());
        assertEquals("vehicle.active", config.getSelectionCriteria().get().get(0));
        assertEquals("vehicle.desc", config.getSelectionCriteria().get().get(1));
        assertEquals("vehicle", config.getSelectionCriteria().get().get(2));
    }

    @Test
    public void selection_criteria_with_all_possible_single_valued_criteria_should_be_constructed_successfully() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("boolSingle").asSingle().bool()
                .also()
                .addCrit("stringSingle").asSingle().text()
                .also()
                .addCrit("intSingle").asSingle().integer()
                .also()
                .addCrit("moneySingle").asSingle().decimal()
                .also()
                .addCrit("bigDecimalSingle").asSingle().decimal()
                .also()
                .addCrit("orgunitCritOnlySingle").asSingle().autocompleter(TgOrgUnit1.class)
                .also()
                .addCrit("dateSingle").asSingle().date()
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals(7, config.getSelectionCriteria().get().size());
        assertEquals("boolSingle", config.getSelectionCriteria().get().get(0));
        assertEquals("stringSingle", config.getSelectionCriteria().get().get(1));
        assertEquals("intSingle", config.getSelectionCriteria().get().get(2));
        assertEquals("moneySingle", config.getSelectionCriteria().get().get(3));
        assertEquals("bigDecimalSingle", config.getSelectionCriteria().get().get(4));
        assertEquals("orgunitCritOnlySingle", config.getSelectionCriteria().get().get(5));
        assertEquals("dateSingle", config.getSelectionCriteria().get().get(6));
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_bool_for_non_boolean_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("intSingle").asSingle().bool()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a boolean component as it is not of type boolean (%s).", "intSingle", TgWorkOrder.class.getSimpleName(), Integer.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_integer_for_non_integer_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("boolSingle").asSingle().integer()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for an integer component as it is not of type Integer (%s).", "boolSingle", TgWorkOrder.class.getSimpleName(), boolean.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_decimal_for_non_decimal_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("intSingle").asSingle().decimal()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a decimal component as it is not of type BigDecimal or Money (%s).", "intSingle", TgWorkOrder.class.getSimpleName(), Integer.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_text_for_non_string_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("intSingle").asSingle().text()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a text component as it is not of type String (%s).", "intSingle", TgWorkOrder.class.getSimpleName(), Integer.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_date_for_non_date_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("intSingle").asSingle().date()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", "intSingle", TgWorkOrder.class.getSimpleName(), Integer.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_autocompleter_with_null_entity_type() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("orgunitCritOnlySingle").asSingle().autocompleter(null)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals("Property type is a required argument and cannot be omitted.", ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_autocompleter_with_non_matching_property_type() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("orgunitCritOnlySingle").asSingle().autocompleter(TgVehicle.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' has type %s, but type %s has been specified instead.", "orgunitCritOnlySingle", TgWorkOrder.class.getSimpleName(), TgOrgUnit1.class.getSimpleName(), TgVehicle.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_valued_crit_as_autocompleter_for_non_entity_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("intSingle").asSingle().autocompleter(TgVehicle.class)
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for autocompletion as it is not of an entity type (%s).", "intSingle", TgWorkOrder.class.getSimpleName(), Integer.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_with_all_possible_range_valued_criteria_should_be_constructed_successfully() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("yearlyCost").asRange().decimal()
                .also()
                .addCrit("vehicle.lastMeterReading").asRange().decimal()
                .also()
                .addCrit("vehicle.constValueProp").asRange().integer()
                .also()
                .addCrit("vehicle.initDate").asRange().date()
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals(4, config.getSelectionCriteria().get().size());
        assertEquals("yearlyCost", config.getSelectionCriteria().get().get(0));
        assertEquals("vehicle.lastMeterReading", config.getSelectionCriteria().get().get(1));
        assertEquals("vehicle.constValueProp", config.getSelectionCriteria().get().get(2));
        assertEquals("vehicle.initDate", config.getSelectionCriteria().get().get(3));
    }

    @Test
    public void selection_criteria_should_not_permit_range_valued_crit_as_date_for_non_date_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("yearlyCost").asRange().date()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", "yearlyCost", TgWorkOrder.class.getSimpleName(), Money.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_range_valued_crit_as_integer_for_non_integer_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("yearlyCost").asRange().integer()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for an integer component as it is not of type Integer (%s).", "yearlyCost", TgWorkOrder.class.getSimpleName(), Money.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_range_valued_crit_as_decimal_for_non_decimal_property() {
        try {
            centreFor(TgWorkOrder.class)
                    .addCrit("vehicle.initDate").asRange().decimal()
                    .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []")
                    .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used for a decimal component as it is not of type BigDecimal or Money (%s).", "vehicle.initDate", TgWorkOrder.class.getSimpleName(), Date.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void should_capture_default_values_for_all_kinds_and_types_of_selection_criteria() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("boolSingle").asSingle().bool().setDefaultValue(single().bool().setValue(true).canHaveNoValue().value())
                .also()
                .addCrit("stringSingle").asSingle().text().setDefaultValue(single().text().setValue("LA*").value())
                .also()
                .addCrit("intSingle").asSingle().integer().setDefaultValue(single().integer().setValue(42).value())
                .also()
                .addCrit("moneySingle").asSingle().decimal().setDefaultValue(single().decimal().setValue(new BigDecimal("42.00")).value())
                .also()
                .addCrit("bigDecimalSingle").asSingle().decimal().setDefaultValue(single().decimal().setValue(new BigDecimal("42.00")).value())
                .also()
                .addCrit("orgunitCritOnlySingle").asSingle().autocompleter(TgOrgUnit1.class).setDefaultValue(single().entity(TgOrgUnit1.class).setValue(new TgOrgUnit1().setKey("ABC")).value()) // there is a check not to permit nulls and non-matching property type values!
                .also()
                .addCrit("dateSingle").asSingle().date().setDefaultValue(single().date().setValue(new Date()).value())
                .also()
                .addCrit("yearlyCost").asRange().decimal().setDefaultValue(range().decimal().setFromValueExclusive(new BigDecimal("42.00")).value())
                .also()
                .addCrit("vehicle.lastMeterReading").asRange().decimal().setDefaultValue(range().decimal().canHaveNoValue().value())
                .also()
                .addCrit("vehicle.constValueProp").asRange().integer().setDefaultValue(range().integer().setToValue(235).value())
                .also()
                .addCrit("vehicle.initDate").asRange().date().setDefaultValue(range().date().not().next().finYear().value())
                .also()
                .addCrit("vehicle.active").asMulti().bool().setDefaultValue(multi().bool().setIsNotValue(true).canHaveNoValue().value())
                .also()
                .addCrit("vehicle.desc").asMulti().text().setDefaultValue(multi().string().not().setValues("des*", "*la-la*").value())
                .also()
                .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class).setDefaultValue(multi().string().setValues("AB*", "*CD*GJH").canHaveNoValue().value())
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals(14, config.getSelectionCriteria().get().size());
        assertEquals("boolSingle", config.getSelectionCriteria().get().get(0));
        assertEquals("stringSingle", config.getSelectionCriteria().get().get(1));
        assertEquals("intSingle", config.getSelectionCriteria().get().get(2));
        assertEquals("moneySingle", config.getSelectionCriteria().get().get(3));
        assertEquals("bigDecimalSingle", config.getSelectionCriteria().get().get(4));
        assertEquals("orgunitCritOnlySingle", config.getSelectionCriteria().get().get(5));
        assertEquals("dateSingle", config.getSelectionCriteria().get().get(6));
        assertEquals("yearlyCost", config.getSelectionCriteria().get().get(7));
        assertEquals("vehicle.lastMeterReading", config.getSelectionCriteria().get().get(8));
        assertEquals("vehicle.constValueProp", config.getSelectionCriteria().get().get(9));
        assertEquals("vehicle.initDate", config.getSelectionCriteria().get().get(10));
        assertEquals("vehicle.active", config.getSelectionCriteria().get().get(11));
        assertEquals("vehicle.desc", config.getSelectionCriteria().get().get(12));
        assertEquals("vehicle", config.getSelectionCriteria().get().get(13));

        assertFalse(config.getDefaultSingleValueAssignersForBooleanSelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValuesForBooleanSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValuesForBooleanSelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValueAssignersForStringSelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValuesForStringSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValuesForStringSelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValueAssignersForEntitySelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValuesForEntitySelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValuesForEntitySelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValueAssignersForDateSelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValuesForDateSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValuesForDateSelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria().isPresent());
        assertEquals("Expectign one value for BigDecimal property and one for Money property.", 2, config.getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValueAssignersForIntegerSelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValuesForIntegerSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValuesForIntegerSelectionCriteria().get().size());

        assertFalse(config.getDefaultMultiValueAssignersForBooleanSelectionCriteria().isPresent());
        assertTrue(config.getDefaultMultiValuesForBooleanSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultMultiValuesForBooleanSelectionCriteria().get().size());
        assertFalse(config.getDefaultMultiValueAssignersForEntityAndStringSelectionCriteria().isPresent());
        assertTrue(config.getDefaultMultiValuesForEntityAndStringSelectionCriteria().isPresent());
        assertEquals("Expecting one value for entity property and one for string property", 2, config.getDefaultMultiValuesForEntityAndStringSelectionCriteria().get().size());

        assertFalse(config.getDefaultRangeValueAssignersForDateSelectionCriteria().isPresent());
        assertTrue(config.getDefaultRangeValuesForDateSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultRangeValuesForDateSelectionCriteria().get().size());
        assertFalse(config.getDefaultRangeValueAssignersForIntegerSelectionCriteria().isPresent());
        assertTrue(config.getDefaultRangeValuesForIntegerSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultRangeValuesForIntegerSelectionCriteria().get().size());
        assertFalse(config.getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria().isPresent());
        assertTrue(config.getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria().isPresent());
        assertEquals("Expecting one value for BigDecimal property and one for Money property", 2, config.getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria().get().size());
    }

    @Test
    public void should_capture_default_value_assigners_for_all_kinds_and_types_of_selection_criteria() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("boolSingle").asSingle().bool().withDefaultValueAssigner(DefaultValueAssignerForSingleBoolean.class)
                .also()
                .addCrit("stringSingle").asSingle().text().withDefaultValueAssigner(DefaultValueAssignerForSingleString.class)
                .also()
                .addCrit("intSingle").asSingle().integer().withDefaultValueAssigner(DefaultValueAssignerForSingleInteger.class)
                .also()
                .addCrit("moneySingle").asSingle().decimal().withDefaultValueAssigner(DefaultValueAssignerForSingleDecimal.class)
                .also()
                .addCrit("bigDecimalSingle").asSingle().decimal().withDefaultValueAssigner(DefaultValueAssignerForSingleDecimal.class)
                .also()
                .addCrit("orgunitCritOnlySingle").asSingle().autocompleter(TgOrgUnit1.class).withDefaultValueAssigner(DefaultValueAssignerForSingleTgOrgUnit.class)
                .also()
                .addCrit("dateSingle").asSingle().date().withDefaultValueAssigner(DefaultValueAssignerForSingleDate.class)
                .also()
                .addCrit("yearlyCost").asRange().decimal().withDefaultValueAssigner(DefaultValueAssignerForRangeDecimal.class)
                .also()
                .addCrit("vehicle.lastMeterReading").asRange().decimal().withDefaultValueAssigner(DefaultValueAssignerForRangeDecimal.class)
                .also()
                .addCrit("vehicle.constValueProp").asRange().integer().withDefaultValueAssigner(DefaultValueAssignerForRangeInteger.class)
                .also()
                .addCrit("vehicle.initDate").asRange().date().withDefaultValueAssigner(DefaultValueAssignerForRangeDate.class)
                .also()
                .addCrit("vehicle.active").asMulti().bool().withDefaultValueAssigner(DefaultValueAssignerForMultiBoolean.class)
                .also()
                .addCrit("vehicle.desc").asMulti().text().withDefaultValueAssigner(DefaultValueAssignerForMultiString.class)
                .also()
                .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class).withDefaultValueAssigner(DefaultValueAssignerForMultiString.class)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals(14, config.getSelectionCriteria().get().size());
        assertEquals("boolSingle", config.getSelectionCriteria().get().get(0));
        assertEquals("stringSingle", config.getSelectionCriteria().get().get(1));
        assertEquals("intSingle", config.getSelectionCriteria().get().get(2));
        assertEquals("moneySingle", config.getSelectionCriteria().get().get(3));
        assertEquals("bigDecimalSingle", config.getSelectionCriteria().get().get(4));
        assertEquals("orgunitCritOnlySingle", config.getSelectionCriteria().get().get(5));
        assertEquals("dateSingle", config.getSelectionCriteria().get().get(6));
        assertEquals("yearlyCost", config.getSelectionCriteria().get().get(7));
        assertEquals("vehicle.lastMeterReading", config.getSelectionCriteria().get().get(8));
        assertEquals("vehicle.constValueProp", config.getSelectionCriteria().get().get(9));
        assertEquals("vehicle.initDate", config.getSelectionCriteria().get().get(10));
        assertEquals("vehicle.active", config.getSelectionCriteria().get().get(11));
        assertEquals("vehicle.desc", config.getSelectionCriteria().get().get(12));
        assertEquals("vehicle", config.getSelectionCriteria().get().get(13));

        assertTrue(config.getDefaultSingleValueAssignersForBooleanSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValueAssignersForBooleanSelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValuesForBooleanSelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValueAssignersForStringSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValueAssignersForStringSelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValuesForStringSelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValueAssignersForEntitySelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValueAssignersForEntitySelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValuesForEntitySelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValueAssignersForDateSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValueAssignersForDateSelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValuesForDateSelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria().isPresent());
        assertEquals("Expectign one value assinger for BigDecimal property and one for Money property.", 2, config.getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria().isPresent());
        assertTrue(config.getDefaultSingleValueAssignersForIntegerSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultSingleValueAssignersForIntegerSelectionCriteria().get().size());
        assertFalse(config.getDefaultSingleValuesForIntegerSelectionCriteria().isPresent());

        assertTrue(config.getDefaultMultiValueAssignersForBooleanSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultMultiValueAssignersForBooleanSelectionCriteria().get().size());
        assertFalse(config.getDefaultMultiValuesForBooleanSelectionCriteria().isPresent());
        assertTrue(config.getDefaultMultiValueAssignersForEntityAndStringSelectionCriteria().isPresent());
        assertEquals("Expecting one value assigners for entity property and one for string property", 2, config.getDefaultMultiValueAssignersForEntityAndStringSelectionCriteria().get().size());
        assertFalse(config.getDefaultMultiValuesForEntityAndStringSelectionCriteria().isPresent());

        assertTrue(config.getDefaultRangeValueAssignersForDateSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultRangeValueAssignersForDateSelectionCriteria().get().size());
        assertFalse(config.getDefaultRangeValuesForDateSelectionCriteria().isPresent());
        assertTrue(config.getDefaultRangeValueAssignersForIntegerSelectionCriteria().isPresent());
        assertEquals(1, config.getDefaultRangeValueAssignersForIntegerSelectionCriteria().get().size());
        assertFalse(config.getDefaultRangeValuesForIntegerSelectionCriteria().isPresent());
        assertTrue(config.getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria().isPresent());
        assertEquals("Expecting one value assigners for BigDecimal property and one for Money property", 2, config.getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria().get().size());
        assertFalse(config.getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria().isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_single_bool_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("boolSingle").asSingle().bool().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_single_string_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("stringSingle").asSingle().text().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_single_integer_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("intSingle").asSingle().integer().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_single_deciamal_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("moneySingle").asSingle().decimal().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_single_entity_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("orgunitCritOnlySingle").asSingle().autocompleter(TgOrgUnit1.class).setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_single_date_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("dateSingle").asSingle().date().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_range_decimal_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("yearlyCost").asRange().decimal().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_vaule_for_range_integer_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("vehicle.constValueProp").asRange().integer().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_range_date_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("vehicle.initDate").asRange().date().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_multi_bool_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("vehicle.active").asMulti().bool().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_multi_text_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("vehicle.desc").asMulti().text().setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_default_value_for_multi_entity_selection_crit_is_not_permitted() {
        centreFor(TgWorkOrder.class)
                .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class).setDefaultValue(null)
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', [][][]")
                .addProp("desc").build();
    }

    @Test
    public void completed_dsl_statement_should_selection_criteria_for_at_least_one_device() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("yearlyCost").asRange().decimal()
                .setLayoutFor(DESKTOP, Optional.of(LANDSCAPE), "['vertical', 'justified', 'margin:20px', []]")
                .addProp("desc").build();

        assertNotNull(config.getSelectionCriteriaLayout());
        assertTrue(config.getSelectionCriteriaLayout().hasLayoutFor(DESKTOP, LANDSCAPE));
    }

}
