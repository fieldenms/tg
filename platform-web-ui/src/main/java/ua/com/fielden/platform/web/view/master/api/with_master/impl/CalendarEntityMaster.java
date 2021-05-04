package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import static ua.com.fielden.platform.utils.CollectionUtil.linkedSetOf;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.CalendarEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * An entity master for {@CalendarEntity}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class CalendarEntityMaster implements IMaster<CalendarEntity> {

    private final IRenderable renderable;

    public CalendarEntityMaster(final String eventTitleProp, final String eventFromProp, final String eventToProp) {
        final DomElement calendar = new DomElement("tg-fullcalendar")
                .attr("id", "calendar")
                .attr("custom-event-target", "[[customEventTarget]]")
                .attr("entities", "[[retrievedEntities]]")
                .attr("event-title-property", "\"" + eventTitleProp + "\"")
                .attr("event-from-property", "\"" + eventFromProp + "\"")
                .attr("event-to-property", "\"" + eventToProp + "\"")
                .attr("uuid", "[[centreUuid]]")
                .attr("lock", "[[lock]]");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, createImports(linkedSetOf("components/fullcalendar/tg-fullcalendar")) +
                        "\nimport { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';\n")
                .replace(ENTITY_TYPE, flattenedNameOf(CalendarEntity.class))
                .replace("<!--@tg-entity-master-content-->", calendar.toString())
                .replace("//generatedPrimaryActions", "")
                .replace("//@ready-callback", readyCallback())
                .replace("@prefDim", "null")
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
        return "self.classList.remove('canLeave');\n"
                + "self._focusFirstInput = function () {};\n";
//                +"//Locks/Unlocks tg-planning's lock layer during insertion point activation\n"
//                + "self.disableViewForDescendants = function () {\n"
//                + "    TgEntityBinderBehavior.disableViewForDescendants.call(this);\n"
//                + "    self.lock = true;\n"
//                + "    self.showDataLoadingPromt();\n"
//                + "};\n"
//                + "self.enableViewForDescendants = function () {\n"
//                + "    TgEntityBinderBehavior.enableViewForDescendants.call(this);\n"
//                + "    self.lock = false;"
//                + "    self.showDataLoadedPromt();\n"
//                + "};\n"
//                + "self.showDataLoadingPromt = function () {\n"
//                + "    this._toastGreeting().text = 'Loading manpower resource planning data...';\n"
//                + "    this._toastGreeting().hasMore = false;\n"
//                + "    this._toastGreeting().showProgress = true;\n"
//                + "    this._toastGreeting().msgHeading = 'Info';\n"
//                + "    this._toastGreeting().isCritical = false;\n"
//                + "    this._toastGreeting().show();\n"
//                + "};\n"
//                + "self.showDataLoadedPromt = function () {\n"
//                + "    this._toastGreeting().text = 'Loading completed successfully';\n"
//                + "    this._toastGreeting().hasMore = false;\n"
//                + "    this._toastGreeting().showProgress = false;\n"
//                + "    this._toastGreeting().msgHeading = 'Info';\n"
//                + "    this._toastGreeting().isCritical = false;\n"
//                + "    this._toastGreeting().show();\n"
//                + "};\n";
    }

    @Override
    public IRenderable render() {
        return renderable;
    }


    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<CalendarEntity, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

}
