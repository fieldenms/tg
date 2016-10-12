package ua.com.fielden.platform.web.test.server;

import java.util.LinkedHashSet;
import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.sample.domain.TgPolygonMap;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * An entity master that represents a chart for {@link TgPolygonMapMaster}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class TgPolygonMapMaster implements IMaster<TgPolygonMap> {

    private final IRenderable renderable;

    public TgPolygonMapMaster() {
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        // importPaths.add("gis/tg-gis-component");
        // importPaths.add("gis/message/tg-message-gis-component");
        importPaths.add("gis/polygon/tg-polygon-gis-component");
        // importPaths.add("gis/realtimemonitor/tg-realtime-monitor-gis-component");
        // importPaths.add("gis/stop/tg-stop-gis-component");
        importPaths.add("gis/tg-map");

        final int funcActionSeq = 0; // used for both entity and property level functional actions
        final String prefix = ",\n";
        final int prefixLength = prefix.length();
        final StringBuilder primaryActionObjects = new StringBuilder();
        final DomElement tgPolygonMap = new DomElement("tg-map")
                .clazz("tg-map")
                .attr("entity", "[[_currBindingEntity]]")
                .attr("column-properties-mapper", "{{columnPropertiesMapper}}")
                .attr("retrieved-entity-selection", "{{retrievedEntitySelection}}")
                .attr("retrieved-entities", "{{retrievedEntities}}")
                .attr("retrieved-totals", "{{retrievedTotals}}");

        final String primaryActionObjectsString = primaryActionObjects.toString();

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", TgPolygonMap.class.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", tgPolygonMap.toString())
                .replace("//generatedPrimaryActions", primaryActionObjectsString.length() > prefixLength ? primaryActionObjectsString.substring(prefixLength)
                        : primaryActionObjectsString)
                // use: L.GIS.GisComponent, L.GIS.MessageGisComponent, L.GIS.PolygonGisComponent, L.GIS.RealtimeMonitorGisComponent, L.GIS.StopGisComponent
                .replace("//@attached-callback", "self.classList.remove('canLeave'); "
                        + "new L.GIS.PolygonGisComponent(self.querySelector('.map'), self.querySelector('.progress'), self.querySelector('.progress-bar'), self.querySelector('.tg-map'));")
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
    public Optional<Class<? extends IValueMatcherWithContext<TgPolygonMap, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }
    
    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }
}
