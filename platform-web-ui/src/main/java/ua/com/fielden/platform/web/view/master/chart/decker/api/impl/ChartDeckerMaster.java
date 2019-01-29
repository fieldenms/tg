package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.join;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getTimePortionToDisplay;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getTimeZone;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.PRIMARY_RESULT_SET;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
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
                .replace(IMPORTS, createImports(importPaths))
                .replace(ENTITY_TYPE, flattenedNameOf(deckerConfig.getEntityType()))
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
        final List<ChartDeck<T>> decs = deckerConfig.getDecs();
        for (int deckIndex = 0; deckIndex < decs.size(); deckIndex++) {
            final List<ChartSeries<T>> series = decs.get(deckIndex).getSeries();
            for (int seriesIndex = 0; seriesIndex < series.size(); seriesIndex++) {
                final ChartSeries<T> s = series.get(seriesIndex);
                final EntityActionConfig config = s.getAction();
                this.actions.add(config);
                if (config != null && !config.isNoAction()) {
                    final FunctionalActionElement el = FunctionalActionElement.newPropertyActionForMaster(config, deckIndex, s.getPropertyName());
                    importPaths.add(el.importPath());
                    container.add(el.render().clazz("chart-action").attr("hidden", true).attr("action-index", seriesIndex).attr("deck-index", deckIndex));
                    primaryActionObjects.add(el.createActionObject());
                }
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
                    .attr("show-legend", charts.get(chartIndex).isShowLegend())
                    .attr("legend-items", "[[legendItems." + chartIndex + "]]")
                    .attr("line-legend-items", "[[lineLegendItems." + chartIndex + "]]")
                    .attr("data", "[[retrievedEntities]]")
                    .attr("options", "[[barOptions." + chartIndex + "]]"));
        }
        return container;
    }

    private String readyCallback(final IChartDeckerConfig<T> deckerConfig) {
        final List<String> chartOptions = new ArrayList<>();
        final List<String> legendItems = new ArrayList<>();
        final List<String> lineLegendItems = new ArrayList<>();
        for (int deckIndex = 0; deckIndex < deckerConfig.getDecs().size(); deckIndex++) {
            final ChartDeck<T> deck = deckerConfig.getDecs().get(deckIndex);
            legendItems.add(generateListOfValues(deck.getSeries(), s -> generateLegendItem(s)));
            lineLegendItems.add(generateListOfValues(deck.getLines(), l -> generateLineLegendItem(l)));
            chartOptions.add("{\n"
                    + "    mode: d3.barChart.BarMode." + deck.getMode().name() + ",\n"
                    + "    label: '" + deck.getTitle() + "',\n"
                    + "    xAxis: {\n"
                    + "        label: '" + deck.getXAxisTitle() + "',\n"
                    + "        orientation: d3.barChart.LabelOrientation." + deck.getxAxisLabelOrientation().name() + "\n"
                    + "    },\n"
                    + "    yAxis: {\n"
                    + "        label: '" + deck.getYAxisTitle() + "'\n"
                    + "    },\n"
                    + "    lines: " + generateListOfValues(deck.getLines(), l -> generateLine(l)) + ",\n"
                    + "    dataPropertyNames: {\n"
                    + "        groupKeyProp: " + generateValueAccessor(deck.getEntityType(), deck.getPropertyType(), deck.getGroupKeyProp()) + ",\n"
                    + "        groupDescProp: '" + deck.getGroupDescProperty() + "',\n"
                    + "        valueProps: " + generateListOfValues(deck.getSeries(), s -> generateValueAccessor(s.getEntityType(), s.getPropertyType(), s.getPropertyName())) + "\n"
                    + "    },\n"
                    + "    colours: " + generateListOfValues(deck.getSeries(), s -> "'" + s.getColour().getColourValue() + "'") + ",\n"
                    + "    barColour: (d, i) => self.barOptions[" + deckIndex + "].colours[i],\n"
                    + "    propertyNames: " + generateListOfValues(deck.getSeries(), s -> "'" + s.getPropertyName() + "'") + ",\n"
                    + "    propertyTypes: " + generateListOfValues(deck.getSeries(), s -> "'" + s.getPropertyType().getSimpleName() + "'") + ",\n"
                    + "    barLabel: (d, i) => this._labelFormatter(d, i, self.barOptions[" + deckIndex + "].propertyNames, self.barOptions[" + deckIndex + "].propertyTypes, self.barOptions[" + deckIndex + "].mode),\n"
                    + "    tooltip: (d, i) => this._tooltip(d, "
                                + generateValueAccessor(deck.getEntityType(), deck.getPropertyType(), deck.getGroupKeyProp()) + ", "
                                + "self.barOptions[" + deckIndex + "].propertyNames[i], "
                                + "self.barOptions[" + deckIndex + "].propertyTypes[i], "
                                + "self.legendItems[" + deckIndex + "][i].title, " + deckIndex + ", i),\n"
                    + "    click: this._click(" + deckIndex + ")\n"
                    + "}");
        }
        return "self.barOptions = [" + join(chartOptions, ",\n") + "];\n"
                + "self.legendItems =[" + join(legendItems, ",\n") + "];\n"
                + "self.lineLegendItems =[" + join(lineLegendItems, ",\n") + "];\n";
    }

    private String generateLineLegendItem(final ChartLine<T> line) {
        return "{title: '"  + line.getTitle() + "', colour: '" + line.getColour().getColourValue() + "'}";
    }

    private String generateLine(final ChartLine<T> line) {
        return "{property: " + generateValueAccessor(line.getEntityType(), line.getPropertyType(), line.getProperty()) + ", title: '"  + line.getTitle() + "', colour: '" + line.getColour().getColourValue() + "'}";
    }

    private <C> String generateListOfValues(final List<C> series, final Function<C, String> func) {
        final StringBuilder items = new StringBuilder("[");
        series.stream().forEach(nextSeries -> {
            items.append(func.apply(nextSeries) + ",");
        });
        if (items.length() > 1) {
            items.setCharAt(items.length() - 1, ']');
        } else {
            items.append(']');
        }
        return items.toString();
    }

    private String generateLegendItem(final ChartSeries<T> series) {
        return "{title: '"  + series.getTitle() + "', colour: '" + series.getColour().getColourValue() + "'}";
    }

    private String generateValueAccessor(final Class<?> deckType, final Class<?> propertyType, final String aggregationProperty) {
        if (Money.class.isAssignableFrom(propertyType)) {
            return "this._moneyPropAccessor('" + aggregationProperty + "')";
        } else if (EntityUtils.isDate(propertyType)) {
            final Optional<String> timeZone = ofNullable(getTimeZone(deckType, aggregationProperty));
            final Optional<String> timePortion = ofNullable(getTimePortionToDisplay(deckType, aggregationProperty));
            final String typeSpec = "Date:" + timeZone.orElse(":") + timePortion.orElse("");
            return "this._datePropAccessor('" + aggregationProperty + "', '" + typeSpec + "')";
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
