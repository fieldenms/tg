package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.PRIMARY_RESULT_SET;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerConfig;

public class ChartDeckerMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;
    private final List<EntityActionConfig> actions = new ArrayList<>();

    public ChartDeckerMaster(final IChartDeckerConfig<T> deckerConfig) {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("components/tg-bar-chart/tg-bar-chart");

        final DomElement decks = createDeckElements(deckerConfig);
        final Pair<String, DomElement> actions = generateActions(deckerConfig, importPaths);
        decks.add(actions.getValue());

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/components/chart-decker/tg-chart-decker-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", deckerConfig.getEntityType().getSimpleName())
                .replace("<!--@tg-entity-master-content-->", decks.toString())
                .replace("//generatedPrimaryActions", actions.getKey())
                .replace("//@ready-callback", readyCallback(deckerConfig))
                .replace("@prefDim", "null").replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", String.valueOf(deckerConfig.shouldSaveOnActivation()));

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private Pair<String, DomElement> generateActions(final IChartDeckerConfig<T> deckerConfig, final LinkedHashSet<String> importPaths) {
        final DomElement container = new DomContainer();
        final List<String> primaryActionObjects = new ArrayList<>();
        for (int deckIndex = 0; deckIndex < deckerConfig.getDecs().size(); deckIndex++) {
            final EntityActionConfig config = deckerConfig.getDecs().get(deckIndex).getAction();
            this.actions.add(config);
            if (config != null && !config.isNoAction()) {
                final FunctionalActionElement el = FunctionalActionElement.newEntityActionForMaster(config, deckIndex);
                importPaths.add(el.importPath());
                container.add(el.render().clazz("chart-action").attr("hidden", true));
                primaryActionObjects.add(el.createActionObject());
            }
        }
        return new Pair<>(StringUtils.join(primaryActionObjects, ",\n"), container);
    }

    private DomElement createDeckElements(final IChartDeckerConfig<T> deckerConfig) {
        final DomElement container = new DomElement("div").clazz("layout", "vertical").style("width:100%", "height:100%");
        final List<ChartDeck<T>> charts = deckerConfig.getDecs();
        for (int chartIndex = 0; chartIndex < charts.size(); chartIndex++) {
            container.add(new DomElement("tg-bar-chart")
                    .clazz("flex", "chart-deck")
                    .attr("data", "[[retrievedEntities]]")
                    .attr("options", "[[barOptions." + chartIndex + "]]"));
        }
        return container;
    }

    private String readyCallback(final IChartDeckerConfig<T> deckerConfig) {
        final List<String> chartOptions = new ArrayList<>();
        for (int deckIndex = 0; deckIndex < deckerConfig.getDecs().size(); deckIndex++) {
            final ChartDeck<T> deck = deckerConfig.getDecs().get(deckIndex);
            chartOptions.add("{\n"
                    + "    label: '" + deck.getTitle() + "',\n"
                    + "    xAxis: {\n"
                    + "        label: '" + deck.getXAxisTitle() + "'\n"
                    + "    },\n"
                    + "    yAxis: {\n"
                    + "        label: '" + deck.getYAxisTitle() + "'\n"
                    + "    },\n"
                    + "    dataPropertyNames: {\n"
                    + "        groupKeyProp: '" + deck.getGroupKeyProp() + "',\n"
                    + "        groupDescProp: '" + deck.getGroupDescProperty() + "',\n"
                    + "        valueProp: " + generateValueAccessor(deck.getPropertyType(), deck.getAggregationProperty()) + "\n"
                    + "    },\n"
                    + "    barColour: d => '" + deck.getBarColour() + "',\n"
                    + "    barLabel: this._labelFormatter('" + deck.getPropertyType().getSimpleName() + "', '" + deck.getAggregationProperty() + "'),\n"
                    + "    tooltip: " + generateTooltipRetriever(deck, deckIndex) + (deck.getAction() != null ? ",\n" : "\n")
                    + (deck.getAction() != null ? "    click: this._click(" + deckIndex + ")\n" : "")
                    + "}");
        }
        return "self.barOptions = [" + StringUtils.join(chartOptions, ",\n") + "];\n";
    }

    private String generateTooltipRetriever(final ChartDeck<T> deck, final int deckIndex) {
        return "this._tooltip('" + deck.getGroupDescProperty() + "', '" + deck.getAggregationProperty() + "', '"
                + deck.getPropertyType().getSimpleName() + "', this.actions[" + deckIndex + "])";
    }

    private String generateValueAccessor(final Class<?> propertyType, final String aggregationProperty) {
        if (Money.class.isAssignableFrom(propertyType)) {
            return "this._moneyPropAccessor('" + aggregationProperty + "')";
        }
        return "'" + aggregationProperty + "'";
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        if (PRIMARY_RESULT_SET.equals(actionKind)) {
            return this.actions.get(actionNumber);
        }
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

}
