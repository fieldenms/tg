package ua.com.fielden.platform.web.view.master.api.impl;

import java.util.LinkedHashSet;
import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * Abstract {@link IMaster} implementation that represents Leaflet-based GIS component with concrete implementation.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractMapMaster<T extends AbstractFunctionalEntityWithCentreContext<String>> implements IMaster<T> {
    private final IRenderable renderable;

    public AbstractMapMaster(final Class<T> entityType, final String gisComponentImportPath, final String gisComponentName) {
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("gis/tg-map");
        importPaths.add(gisComponentImportPath);

        final int funcActionSeq = 0; // used for both entity and property level functional actions
        final String prefix = ",\n";
        final int prefixLength = prefix.length();
        final StringBuilder primaryActionObjects = new StringBuilder();
        final DomElement tgMessageMap = new DomElement("tg-map")
                .clazz("tg-map")
                .attr("entity", "[[_currBindingEntity]]")
                .attr("column-properties-mapper", "{{columnPropertiesMapper}}")
                .attr("retrieved-entity-selection", "{{retrievedEntitySelection}}")
                .attr("egi-selection", "{{egiSelection}}")
                .attr("retrieved-entities", "{{retrievedEntities}}")
                .attr("retrieved-totals", "{{retrievedTotals}}");

        final String primaryActionObjectsString = primaryActionObjects.toString();

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", entityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", tgMessageMap.toString())
                .replace("//generatedPrimaryActions", primaryActionObjectsString.length() > prefixLength ? primaryActionObjectsString.substring(prefixLength)
                        : primaryActionObjectsString)
                .replace("//@attached-callback",
                        "self.classList.remove('canLeave');\n"
                        + "self.querySelector('.tg-map').initialiseOrInvalidate(L.GIS." + gisComponentName + ");\n")
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

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }
    
    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }
}
