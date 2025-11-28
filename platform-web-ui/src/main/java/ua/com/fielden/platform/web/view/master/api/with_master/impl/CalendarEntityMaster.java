package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.PRIMARY_RESULT_SET;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Stream;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * An entity master that contains a calendar view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class CalendarEntityMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;
    private final  EntityActionConfig editAction;

    /**
     * Creates {@link CalendarEntityMaster} for concrete entity type with from / to date properties.
     * 
     * @param entityType
     * @param calendarComponentUri -- 'tg-fullcalendar' component URI or URI of the component that extends 'tg-fullcalendar'
     * @param eventKeyProp -- property name to be displayed as titles in calendar events and tooltips
     * @param eventDescProp -- property name to be displayed as descriptions in calendar events and tooltips
     * @param eventFromProp -- property name to be used as start date of calendar event
     * @param eventToProp -- property name to be used as finish date of calendar event
     * @param colorProp -- property name to be used as background colour of calendar event
     * @param colorTitleProp -- property name to be used as title of calendar event's colour
     * @param colorDescProp -- property name to be used as description of calendar event's colour
     * @param editAction -- action to edit calendar events
     */
    public CalendarEntityMaster(
            final Class<T> entityType,
            final String calendarComponentUri,
            final String eventKeyProp,
            final Optional<String> eventDescProp,
            final String eventFromProp,
            final String eventToProp,
            final String colorProp,
            final String colorTitleProp,
            final String colorDescProp,
            final EntityActionConfig editAction) {

        this.editAction = editAction;

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add(calendarComponentUri);

        final DomElement calendar = new DomElement(calendarComponentUri.substring(calendarComponentUri.lastIndexOf("/") + 1))
                .attr("id", "calendar")
                .attr("custom-event-target", "[[customEventTarget]]")
                .attr("context-retriever", "[[contextRetriever]]")
                .attr("entities", "[[retrievedEntities]]")
                .attr("centre-state", "[[centreState]]")
                .attr("data-change-reason", "[[dataChangeReason]]")
                .attr("event-key-property", eventKeyProp)
                .attr("event-desc-property", eventDescProp.orElse(""))
                .attr("event-from-property", eventFromProp)
                .attr("event-to-property", eventToProp)
                .attr("color-property", colorProp)
                .attr("color-title-property", colorTitleProp)
                .attr("color-desc-property", colorDescProp);

        final FunctionalActionElement el = FunctionalActionElement.newEntityActionForMaster(editAction, 0);
        importPaths.add(el.importPath());
        calendar.add(el.render().attr("hidden", true).clazz("primary-action").attr("slot", "calendar-action"));
        final String editActionObjectString = el.createActionObject();

        final StringBuilder prefDimBuilder = new StringBuilder();
        prefDimBuilder.append("{'width': function() {return '100%'}, 'height': function() {return '100%'}, 'widthUnit': '', 'heightUnit': ''}");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, createImports(importPaths) +
                        "\nimport { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';\n")
                .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                .replace("<!--@tg-entity-master-content-->", calendar.toString())
                .replace("//generatedPrimaryActions", editActionObjectString)
                .replace("//@ready-callback", readyCallback())
                .replace("@prefDim", prefDimBuilder.toString())
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private String readyCallback() {
        return "self.uuid = self.centreUuid;\n"
                + "self.classList.remove('canLeave');\n"
                + "self._focusFirstInput = function () {};\n";
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public Stream<EntityActionConfig> streamActionConfigs() {
        return Stream.of(editAction);
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        if (PRIMARY_RESULT_SET == actionKind && actionNumber == 0) {
            return editAction;
        }
        throw new UnsupportedOperationException("Getting of other kind and number action configuration is not supported, except PRIMARY_RESULT_SET and 0 action number");
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return empty();
    }

}
