package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import static ua.com.fielden.platform.utils.CollectionUtil.linkedSetOf;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * An entity master for that contains calendar view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class CalendarEntityMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public CalendarEntityMaster(
            final Class<T> entityType,
            final String eventKeyProp,
            final String eventDescProp,
            final String eventFromProp,
            final String eventToProp,
            final String colorProp,
            final String colorTitleProp,
            final String colorDescProp) {
        final DomElement calendar = new DomElement("tg-fullcalendar")
                .attr("id", "calendar")
                .attr("custom-event-target", "[[customEventTarget]]")
                .attr("context-retriever", "[[contextRetriever]]")
                .attr("entities", "[[retrievedEntities]]")
                .attr("centre-state", "[[centreState]]")
                .attr("event-key-property", eventKeyProp)
                .attr("event-desc-property", StringUtils.isEmpty(eventDescProp) ? "" : eventDescProp)
                .attr("event-from-property", eventFromProp)
                .attr("event-to-property", eventToProp)
                .attr("color-property", colorProp)
                .attr("color-title-property", colorTitleProp)
                .attr("color-desc-property", colorDescProp)
                .attr("uuid", "[[centreUuid]]");

        final StringBuilder prefDimBuilder = new StringBuilder();
        prefDimBuilder.append("{'width': function() {return '100%'}, 'height': function() {return '100%'}, 'widthUnit': '', 'heightUnit': ''}");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, createImports(linkedSetOf("components/fullcalendar/tg-fullcalendar")) +
                        "\nimport { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';\n")
                .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                .replace("<!--@tg-entity-master-content-->", calendar.toString())
                .replace("//generatedPrimaryActions", "")
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
        return "self.classList.remove('canLeave');\n"
                + "self._focusFirstInput = function () {};\n";
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
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

}
