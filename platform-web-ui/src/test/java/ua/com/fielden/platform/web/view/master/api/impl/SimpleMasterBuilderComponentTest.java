package ua.com.fielden.platform.web.view.master.api.impl;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
import ua.com.fielden.platform.web.centre.api.actions.multi.SingleActionSelector;
import ua.com.fielden.platform.web.centre.api.impl.helpers.FunctionalEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.PRIMARY_RESULT_SET;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector.ERR_COMPONENT_FOR_MISSING_PROPERTY;
import static ua.com.fielden.platform.web.view.master.api.widgets.component.impl.AbstractComponentWidget.*;

/// Tests for application-provided web components in entity masters:
/// in place of an editor, declared with `asComponent`, and bound to the entity, declared with `addComponent`.
///
/// Masters are built and rendered without an injector, and assertions are made against the generated master.
///
public class SimpleMasterBuilderComponentTest {

    private static final String PROPERTY_COMPONENT = "components/tg-fuel-chart";
    private static final String ENTITY_COMPONENT = "components/tg-vehicle-fuel-usages";
    private static final String PROPERTY_COMPONENT_DESC = "component [components/tg-fuel-chart] for property [TgPersistentEntityWithProperties.integerProp]";
    private static final String ENTITY_COMPONENT_DESC = "component [components/tg-vehicle-fuel-usages] in the entity master for [TgPersistentEntityWithProperties]";
    /// The modification state and the context of the master, rendered by both component widgets after their own attributes.
    private static final String MASTER_STATE_ATTRS = "entity-modified='[[_bindingEntityModified]]' entity-edited='[[_editedPropsExist]]' create-context-holder='[[_createContextHolder]]'";

    /// A selector distinct from [SingleActionSelector], to tell the selectors of different widgets apart.
    ///
    public static class SecondActionSelector implements IEntityMultiActionSelector {
        @Override
        public int getActionFor(final AbstractEntity<?> entity) {
            return 1;
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // Component in place of an editor
    ////////////////////////////////////////////////////////////////////////

    @Test
    public void property_component_is_imported_and_bound_to_the_fully_fledged_entity_and_to_its_property() {
        final String out = render(master().addProp("integerProp").asComponent(PROPERTY_COMPONENT).also());

        assertTrue(out.contains("import '/resources/components/tg-fuel-chart.js';"));
        assertTrue(out.contains("<tg-fuel-chart id='component_4_integerProp' entity='[[_currEntity]]' property-name='integerProp' prop-title='Integer prop' prop-desc='Integer prop desc' property-action-index='[[_propertyActionIndices.integerProp]]' block-when-unsaved " + MASTER_STATE_ATTRS + ">"));
    }

    @Test
    public void property_component_element_name_can_differ_from_the_last_segment_of_its_import_path() {
        final String out = render(master().addProp("integerProp").asComponent(PROPERTY_COMPONENT).withElementName("tg-fuel-usage-chart").also());

        assertTrue(out.contains("import '/resources/components/tg-fuel-chart.js';"));
        assertTrue(out.contains("<tg-fuel-usage-chart id='component_4_integerProp' "));
        assertFalse(out.contains("<tg-fuel-chart "));
    }

    @Test
    public void declared_attributes_follow_platform_attributes_in_order_of_first_declaration_and_repeated_declaration_replaces_value() {
        final String out = render(master().addProp("integerProp").asComponent(PROPERTY_COMPONENT)
                .withAttr("mode", "compact")
                .withAttr("centre-uuid", "[[centreUuid]]")
                .withAttr("mode", "full")
                .also());

        assertTrue(out.contains(MASTER_STATE_ATTRS + " mode='full' centre-uuid='[[centreUuid]]'>"));
    }

    @Test
    public void property_component_that_skips_blocking_has_no_blocking_flag_but_receives_modification_state_of_master() {
        final String out = render(master().addProp("integerProp").asComponent(PROPERTY_COMPONENT).skipBlockingWhenUnsaved().also());

        assertTrue(out.contains("property-action-index='[[_propertyActionIndices.integerProp]]' " + MASTER_STATE_ATTRS + ">"));
        assertFalse(out.contains("block-when-unsaved"));
    }

    @Test
    public void property_component_action_is_rendered_as_property_action_of_its_property() {
        final EntityActionConfig propAction = mkAction("propAction");
        final IMaster<TgPersistentEntityWithProperties> master = done(master().addProp("integerProp").asComponent(PROPERTY_COMPONENT).withAction(propAction).also());
        final String out = render(master);
        final String actionElement = actionElement(out, "propAction");

        assertTrue(actionElement.contains("slot='property-action'"));
        assertTrue(actionElement.contains("chosen-property='integerProp'"));
        assertTrue(actionElement.contains("number-of-action='0'"));
        assertChildOf("tg-fuel-chart", actionElement, out);
        assertSame(propAction, master.actionConfig(PRIMARY_RESULT_SET, 0));
        assertEquals(Map.of("integerProp", SingleActionSelector.class), master.propertyActionSelectors());
    }

    ////////////////////////////////////////////////////////////////////////
    // Component bound to the entity
    ////////////////////////////////////////////////////////////////////////

    @Test
    public void entity_component_is_imported_identified_by_its_component_key_and_bound_to_the_fully_fledged_entity_without_a_property() {
        final String out = render(master().addComponent(ENTITY_COMPONENT).also());

        assertTrue(out.contains("import '/resources/components/tg-vehicle-fuel-usages.js';"));
        assertTrue(out.contains("<tg-vehicle-fuel-usages id='component-0' entity='[[_currEntity]]' property-action-index='[[_propertyActionIndices.component-0]]' block-when-unsaved " + MASTER_STATE_ATTRS + ">"));
    }

    @Test
    public void entity_component_receives_title_and_description_only_if_they_are_declared() {
        final String out = render(master()
                .addComponent(ENTITY_COMPONENT).withTitle("Fuel usages").withDesc("Fuel usages of the vehicle").also()
                .addComponent("components/tg-vehicle-chart").also());

        assertTrue(out.contains("<tg-vehicle-fuel-usages id='component-0' entity='[[_currEntity]]' prop-title='Fuel usages' prop-desc='Fuel usages of the vehicle' property-action-index="));
        assertTrue(out.contains("<tg-vehicle-chart id='component-1' entity='[[_currEntity]]' property-action-index="));
    }

    @Test
    public void entity_component_declares_element_name_attributes_and_blocking_as_property_component_does() {
        final String out = render(master().addComponent(ENTITY_COMPONENT)
                .withElementName("tg-fuel-usages")
                .withAttr("mode", "compact")
                .skipBlockingWhenUnsaved()
                .also());

        assertTrue(out.contains("<tg-fuel-usages id='component-0' entity='[[_currEntity]]' property-action-index='[[_propertyActionIndices.component-0]]' " + MASTER_STATE_ATTRS + " mode='compact'>"));
    }

    @Test
    public void entity_component_action_is_rendered_as_property_action_without_chosen_property() {
        final String out = render(master().addComponent(ENTITY_COMPONENT).withAction(mkAction("componentAction")).also());
        final String actionElement = actionElement(out, "componentAction");

        assertTrue(actionElement.contains("slot='property-action'"));
        assertFalse(actionElement.contains("chosen-property"));
        assertChildOf("tg-vehicle-fuel-usages", actionElement, out);
    }

    ////////////////////////////////////////////////////////////////////////
    // Components among editors
    ////////////////////////////////////////////////////////////////////////

    @Test
    public void components_are_laid_out_among_editors_in_order_of_declaration() {
        final String out = render(master()
                .addProp("stringProp").asSinglelineText().also()
                .addComponent(ENTITY_COMPONENT).also()
                .addProp("integerProp").asComponent(PROPERTY_COMPONENT).also()
                .addProp("bigDecimalProp").asDecimal().also());
        final int textEditor = out.indexOf("<tg-singleline-text-editor ");
        final int entityComponent = out.indexOf("<tg-vehicle-fuel-usages ");
        final int propertyComponent = out.indexOf("<tg-fuel-chart ");
        final int decimalEditor = out.indexOf("<tg-decimal-editor ");

        assertTrue(textEditor >= 0);
        assertTrue(textEditor < entityComponent);
        assertTrue(entityComponent < propertyComponent);
        assertTrue(propertyComponent < decimalEditor);
    }

    @Test
    public void actions_are_numbered_across_property_actions_component_actions_and_entity_actions_in_order_of_declaration() {
        final EntityActionConfig propAction = mkAction("propAction");
        final EntityActionConfig componentAction = mkAction("componentAction");
        final EntityActionConfig firstMultiAction = mkAction("firstMultiAction");
        final EntityActionConfig secondMultiAction = mkAction("secondMultiAction");
        final EntityActionConfig entityAction = mkAction("entityAction");
        final IMaster<TgPersistentEntityWithProperties> master = master()
                .addProp("integerProp").asComponent(PROPERTY_COMPONENT).withAction(propAction).also()
                .addComponent(ENTITY_COMPONENT).withAction(componentAction).also()
                .addComponent("components/tg-vehicle-chart").withMultiAction(multiAction(SecondActionSelector.class, firstMultiAction, secondMultiAction)).also()
                .addAction(entityAction)
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(DESKTOP, empty(), "[]")
                .setLayoutFor(DESKTOP, empty(), "[[]]")
                .done();
        // actions are compared by their short descriptions, because an entity action is recorded as a copy with the role of a button
        final List<String> expected = List.of("propAction", "componentAction", "firstMultiAction", "secondMultiAction", "entityAction");
        final List<String> numbered = expected.stream().map(desc -> master.actionConfig(PRIMARY_RESULT_SET, expected.indexOf(desc)).shortDesc.orElseThrow()).toList();
        final List<String> streamed = master.streamActionConfigs().map(config -> config.shortDesc.orElseThrow()).toList();
        final String out = render(master);

        assertEquals(expected, numbered);
        assertEquals(expected, streamed);
        assertTrue(actionElement(out, "componentAction").contains("number-of-action='1'"));
        assertTrue(actionElement(out, "secondMultiAction").contains("number-of-action='3'"));
        assertTrue(actionElement(out, "entityAction").contains("number-of-action='4'"));
    }

    @Test
    public void property_action_selectors_are_keyed_by_property_names_and_by_component_keys_of_entity_components_with_actions() {
        final IMaster<TgPersistentEntityWithProperties> master = done(master()
                .addProp("integerProp").asComponent(PROPERTY_COMPONENT).withAction(mkAction("propAction")).also()
                .addComponent(ENTITY_COMPONENT).also()
                .addComponent("components/tg-vehicle-chart").withMultiAction(multiAction(SecondActionSelector.class, mkAction("first"), mkAction("second"))).also());
        final String out = render(master);

        assertTrue(out.contains("<tg-vehicle-fuel-usages id='component-0' entity='[[_currEntity]]' property-action-index='[[_propertyActionIndices.component-0]]' "));
        assertTrue(out.contains("<tg-vehicle-chart id='component-1' entity='[[_currEntity]]' property-action-index='[[_propertyActionIndices.component-1]]' "));
        assertEquals(Map.of("integerProp", SingleActionSelector.class, "component-1", SecondActionSelector.class), master.propertyActionSelectors());
    }

    ////////////////////////////////////////////////////////////////////////
    // Validation
    ////////////////////////////////////////////////////////////////////////

    @Test
    public void property_component_requires_an_existing_property() {
        assertConfigurationError(ERR_COMPONENT_FOR_MISSING_PROPERTY.formatted(PROPERTY_COMPONENT, "TgPersistentEntityWithProperties", "integerPropp"),
                () -> master().addProp("integerPropp").asComponent(PROPERTY_COMPONENT));
    }

    @Test
    public void import_path_is_required() {
        assertConfigurationError(ERR_INVALID_IMPORT_PATH.formatted("component [null] in the entity master for [TgPersistentEntityWithProperties]"),
                () -> master().addComponent(null));
    }

    @Test
    public void import_path_cannot_start_with_a_slash() {
        assertConfigurationError(ERR_INVALID_IMPORT_PATH.formatted("component [/components/tg-vehicle-fuel-usages] in the entity master for [TgPersistentEntityWithProperties]"),
                () -> master().addComponent("/components/tg-vehicle-fuel-usages"));
    }

    @Test
    public void import_path_cannot_have_the_js_extension() {
        assertConfigurationError(ERR_INVALID_IMPORT_PATH.formatted("component [components/tg-fuel-chart.js] for property [TgPersistentEntityWithProperties.integerProp]"),
                () -> master().addProp("integerProp").asComponent("components/tg-fuel-chart.js"));
    }

    @Test
    public void import_path_cannot_contain_whitespace() {
        assertConfigurationError(ERR_INVALID_IMPORT_PATH.formatted("component [components/tg vehicle] in the entity master for [TgPersistentEntityWithProperties]"),
                () -> master().addComponent("components/tg vehicle"));
    }

    @Test
    public void element_name_cannot_contain_uppercase_letters() {
        assertConfigurationError(ERR_INVALID_ELEMENT_NAME.formatted("tg-FuelChart", PROPERTY_COMPONENT_DESC),
                () -> master().addProp("integerProp").asComponent(PROPERTY_COMPONENT).withElementName("tg-FuelChart"));
    }

    @Test
    public void element_name_must_contain_a_hyphen() {
        assertConfigurationError(ERR_INVALID_ELEMENT_NAME.formatted("fuelchart", ENTITY_COMPONENT_DESC),
                () -> master().addComponent(ENTITY_COMPONENT).withElementName("fuelchart"));
    }

    @Test
    public void element_name_derived_from_import_path_is_validated_when_the_master_is_built() {
        assertConfigurationError(ERR_INVALID_ELEMENT_NAME.formatted("fuelchart", "component [components/fuelchart] in the entity master for [TgPersistentEntityWithProperties]"),
                () -> render(master().addComponent("components/fuelchart").also()));
    }

    @Test
    public void element_name_derived_from_import_path_may_be_invalid_if_a_valid_element_name_is_declared() {
        final String out = render(master().addComponent("components/fuelchart").withElementName("tg-fuel-chart").also());

        assertTrue(out.contains("<tg-fuel-chart id='component-0' entity='[[_currEntity]]' "));
    }

    @Test
    public void attribute_name_must_be_in_lowercase_dash_case() {
        assertConfigurationError(ERR_INVALID_ATTR_NAME.formatted("centreUuid", ENTITY_COMPONENT_DESC),
                () -> master().addComponent(ENTITY_COMPONENT).withAttr("centreUuid", "[[centreUuid]]"));
    }

    @Test
    public void attributes_rendered_by_both_component_widgets_cannot_be_declared() {
        assertConfigurationError(ERR_RESERVED_ATTR_NAME.formatted("entity", ENTITY_COMPONENT_DESC),
                () -> master().addComponent(ENTITY_COMPONENT).withAttr("entity", "[[_currBindingEntity]]"));
    }

    @Test
    public void context_of_master_rendered_by_both_component_widgets_cannot_be_declared() {
        assertConfigurationError(ERR_RESERVED_ATTR_NAME.formatted("create-context-holder", PROPERTY_COMPONENT_DESC),
                () -> master().addProp("integerProp").asComponent(PROPERTY_COMPONENT).withAttr("create-context-holder", "[[_createContextHolderForEmbeddedViews]]"));
    }

    @Test
    public void id_of_entity_component_is_its_component_key_and_cannot_be_declared() {
        assertConfigurationError(ERR_RESERVED_ATTR_NAME.formatted("id", ENTITY_COMPONENT_DESC),
                () -> master().addComponent(ENTITY_COMPONENT).withAttr("id", "fuelUsages"));
    }

    @Test
    public void attributes_rendered_by_property_component_cannot_be_declared() {
        assertConfigurationError(ERR_RESERVED_ATTR_NAME.formatted("property-name", PROPERTY_COMPONENT_DESC),
                () -> master().addProp("integerProp").asComponent(PROPERTY_COMPONENT).withAttr("property-name", "fuelUsages"));
    }

    @Test
    public void attribute_value_is_required() {
        assertConfigurationError(ERR_NULL_TEXT.formatted("value of attribute [mode]", ENTITY_COMPONENT_DESC),
                () -> master().addComponent(ENTITY_COMPONENT).withAttr("mode", null));
    }

    @Test
    public void attribute_value_cannot_contain_a_straight_single_quote() {
        assertConfigurationError(ERR_UNRENDERABLE_TEXT.formatted("value of attribute [mode]", ENTITY_COMPONENT_DESC, "it's"),
                () -> master().addComponent(ENTITY_COMPONENT).withAttr("mode", "it's"));
    }

    @Test
    public void binding_expression_in_attribute_value_must_have_balanced_square_brackets() {
        assertConfigurationError(ERR_UNBALANCED_BINDING.formatted("centre-uuid", ENTITY_COMPONENT_DESC, "[[centreUuid]"),
                () -> master().addComponent(ENTITY_COMPONENT).withAttr("centre-uuid", "[[centreUuid]"));
    }

    @Test
    public void binding_expression_in_attribute_value_must_have_balanced_curly_braces() {
        assertConfigurationError(ERR_UNBALANCED_BINDING.formatted("selection", ENTITY_COMPONENT_DESC, "{{centreSelection}"),
                () -> master().addComponent(ENTITY_COMPONENT).withAttr("selection", "{{centreSelection}"));
    }

    @Test
    public void title_is_required_if_declared() {
        assertConfigurationError(ERR_NULL_TEXT.formatted("title", ENTITY_COMPONENT_DESC),
                () -> master().addComponent(ENTITY_COMPONENT).withTitle(null));
    }

    @Test
    public void title_cannot_contain_a_backtick() {
        assertConfigurationError(ERR_UNRENDERABLE_TEXT.formatted("title", ENTITY_COMPONENT_DESC, "Fuel `usages`"),
                () -> master().addComponent(ENTITY_COMPONENT).withTitle("Fuel `usages`"));
    }

    @Test
    public void description_cannot_contain_an_interpolation() {
        assertConfigurationError(ERR_UNRENDERABLE_TEXT.formatted("description", ENTITY_COMPONENT_DESC, "Fuel usages in ${year}"),
                () -> master().addComponent(ENTITY_COMPONENT).withDesc("Fuel usages in ${year}"));
    }

    ////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////

    private static IPropertySelector<TgPersistentEntityWithProperties> master() {
        return new SimpleMasterBuilder<TgPersistentEntityWithProperties>().forEntity(TgPersistentEntityWithProperties.class);
    }

    private static IMaster<TgPersistentEntityWithProperties> done(final IPropertySelector<TgPersistentEntityWithProperties> selector) {
        return selector
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(DESKTOP, empty(), "[]")
                .setLayoutFor(DESKTOP, empty(), "[[]]")
                .done();
    }

    private static String render(final IPropertySelector<TgPersistentEntityWithProperties> selector) {
        return render(done(selector));
    }

    private static String render(final IMaster<TgPersistentEntityWithProperties> master) {
        return master.render().render().toString();
    }

    private static EntityActionConfig mkAction(final String shortDesc) {
        return action(FunctionalEntity.class).withContext(context().withMasterEntity().build()).shortDesc(shortDesc).build();
    }

    private static EntityMultiActionConfig multiAction(final Class<? extends IEntityMultiActionSelector> selector, final EntityActionConfig... actions) {
        return new EntityMultiActionConfig(selector, List.of(actions).stream().<Supplier<Optional<EntityActionConfig>>>map(action -> () -> Optional.of(action)).toList());
    }

    /// The rendered element of the functional action with `shortDesc`.
    ///
    private static String actionElement(final String out, final String shortDesc) {
        return out.lines()
                .map(String::strip)
                .filter(line -> line.startsWith("<tg-ui-action ") && line.contains("short-desc='" + shortDesc + "'"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No action element with short description [%s].".formatted(shortDesc)));
    }

    private static void assertChildOf(final String elementName, final String child, final String out) {
        final int childIndex = out.indexOf(child);
        assertTrue(out.indexOf("<" + elementName + " ") < childIndex);
        assertTrue(childIndex < out.indexOf("</" + elementName + ">"));
    }

    private static void assertConfigurationError(final String expectedMessage, final ThrowingRunnable configuration) {
        final EntityMasterConfigurationException ex = assertThrows(EntityMasterConfigurationException.class, configuration);
        assertEquals(expectedMessage, ex.getMessage());
    }

}
