package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerConfig;

public class ChartDeckerMaster<T extends AbstractEntity<?>> implements IMaster<T>{

    private final IRenderable renderable;

    public ChartDeckerMaster(final IChartDeckerConfig<T> deckerConfig) {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("components/tg-bar-chart/tg-bar-chart");

        final DomElement decks = createDeckElements(deckerConfig);

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", deckerConfig.getEntityType().getSimpleName())
                .replace("<!--@tg-entity-master-content-->", decks.toString())
                .replace("//generatedPrimaryActions", "")
                .replace("//@ready-callback", readyCallback(deckerConfig))
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", String.valueOf(deckerConfig.shouldSaveOnActivation()));

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private DomElement createDeckElements(final IChartDeckerConfig<T> deckerConfig) {
        final DomElement container = new DomElement("div")
                .clazz("layout", "vertical")
                .style("width:100%","height:100%");
        final List<ChartDeck<T>> charts = deckerConfig.getDecs();
        for (int chartIndex = 0; chartIndex < charts.size(); chartIndex++) {
            container.add(new DomElement("tg-bar-chart")
                    .clazz("flex")
                    .attr("data", "[[retrievedEntities]]")
                    .attr("options", "[[barOptions." + chartIndex + "]]"));
        }
        return container;
    }

    private String readyCallback(final IChartDeckerConfig<T> deckerConfig) {
        final List<String> chartOptions = new ArrayList<>();
        deckerConfig.getDecs().stream().forEach(deck -> {
            chartOptions.add("{\n"
                    + "    label: '" + deck.getTitle() + "',\n"
                    + "    xAxis: {\n"
                    + "        label: '" + deck.getxAxisTitle()+ "'\n"
                    + "    },\n"
                    + "    yAxis: {\n"
                    + "        label: '" + deck.getyAxisTitle() + "'\n"
                    + "    },\n"
                    + "    dataPropertyNames: {\n"
                    + "        groupKeyProp: '" + deck.getGroupKeyProp() + "',\n"
                    + "        groupDescProp: '" + deck.getGroupDescProperty() + "',\n"
                    + "        valueProp: '" + deck.getAggregationProperty() + "'\n"
                    + "    },\n"
                    + "    barColour: '" + deck.getBarColour() + "'\n"
                    + "}");
        });
        return "self.barOptions = [" + StringUtils.join(chartOptions, ",\n") + "];\n";
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
