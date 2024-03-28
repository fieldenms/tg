package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartLine;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.*;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getTimePortionToDisplay;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getTimeZone;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.PRIMARY_RESULT_SET;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

public class ScatterPlotMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;
    private final List<EntityActionConfig> actions = new ArrayList<>();

    public ScatterPlotMaster(final ScatterPlotMasterBuilder<T> scatterPlotMasterBuilder) {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();

        actions.add(scatterPlotMasterBuilder.getAction());
        final Pair<String, DomElement> actions = generateActions(importPaths);

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/scatter-plot/tg-scatter-plot-master-template.js")
                .replace(IMPORTS, createImports(importPaths))
                .replace(ENTITY_TYPE, flattenedNameOf(scatterPlotMasterBuilder.getEntityType()))
                .replace("<!--@tg-entity-master-content-->", actions.getValue().toString())
                .replace("//generatedPrimaryActions", actions.getKey())
                .replace("//@ready-callback", readyCallback(scatterPlotMasterBuilder))
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", String.valueOf(scatterPlotMasterBuilder.shouldSaveOnActivation()));

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private Pair<String, DomElement> generateActions(final LinkedHashSet<String> importPaths) {
        final DomElement container = new DomContainer();
        final List<String> primaryActionObjects = new ArrayList<>();
        for (int actionIdx = 0; actionIdx < actions.size(); actionIdx++) {
            final EntityActionConfig config = actions.get(actionIdx);
            if (config != null) {
                final FunctionalActionElement el = FunctionalActionElement.newEntityActionForMaster(config, actionIdx);
                importPaths.add(el.importPath());
                container.add(el.render().clazz("chart-action").attr("hidden", true).attr("action-index", actionIdx));
                primaryActionObjects.add(el.createActionObject());
            }
        }
        return new Pair<>(StringUtils.join(primaryActionObjects, ",\n"), container);
    }

    private String readyCallback(final ScatterPlotMasterBuilder<T> chartBuilder) {
        //Generate legend
        final List<String> legendItems = new ArrayList<>();
        chartBuilder.getLegend().forEach(item -> {
            legendItems.add(generateLegendItem(item));
        });
        //Generate options
        final String chartOptions = "{\n"
                +"     margin: {\n" +
                "          left: 100,\n" +
                "          right: 20,\n" +
                "          top: 40,\n" +
                "          bottom: 60\n" +
                "      },"
                + "    label: '" + getChartTitle(chartBuilder) + "',\n"
                + "    xAxis: {\n"
                + "        label: '" + getXAxisTitle(chartBuilder) + "',\n"
                + "        range: self._getValueRange\n"
                + "    },\n"
                + "    yAxis: {\n"
                + "        label: '" + getYAxisTitle(chartBuilder) + "',\n"
                + "        range: self._getCategoryRange\n"
                + "    },\n"
                + "    dataPropertyNames: {\n"
                + "        categoryProp: " + generateValueAccessor(chartBuilder.getChartEntityType(), chartBuilder.getCategoryPropertyName()) + ",\n"
                + "        valueProp: " + generateValueAccessor(chartBuilder.getChartEntityType(), chartBuilder.getValuePropertyName()) + ",\n"
                + "        styleProp: '" + chartBuilder.getStylePropertyName() + "',\n"
                + "    },\n"
                + "    tooltip: d => self._tooltip(d, " + generateTooltipProps(chartBuilder.getTooltipProperties()) + "),\n"
                + "    click: self._click\n"
                + "}";

        return "self.options = " + chartOptions + ";\n"
                + "self.legendItems =[" + join(legendItems, ",\n") + "];\n"
                + "self.categoryRangeSource = '" + chartBuilder.getCategroyRangeConfig().getSource() + "';\n"
                + "self.valueRangeSource = '" + chartBuilder.getValueRangeConfig().getSource() + "';\n";
    }

    private String getYAxisTitle(final ScatterPlotMasterBuilder<T> chartBuilder) {
        return Optional.ofNullable(chartBuilder.getYAxisTitle())
                .orElse(getTitleAndDesc(chartBuilder.getCategoryPropertyName(), chartBuilder.getChartEntityType()).getKey());
    }

    private String getXAxisTitle(final ScatterPlotMasterBuilder<T> chartBuilder) {
        return Optional.ofNullable(chartBuilder.getXAxisTitle())
                .orElse(getTitleAndDesc(chartBuilder.getValuePropertyName(), chartBuilder.getChartEntityType()).getKey());
    }

    private String getChartTitle(final ScatterPlotMasterBuilder<T> chartBuilder) {
        return Optional.ofNullable(chartBuilder.getTitle())
                .orElse(getEntityTitleAndDesc(chartBuilder.getChartEntityType()).getKey());
    }

    private String generateTooltipProps(final List<IConvertableToPath> tooltipProperties) {
        return "[" + tooltipProperties.stream().map(prop -> "'" + prop.toPath() + "'").collect(Collectors.joining(",")) + "]";
    }

    private String generateLegendItem(final Pair<Map<String, String>, String> legendItem) {
        return "{title: '"  + (isEmpty(legendItem.getValue()) ? "" : legendItem.getValue()) + "', style: " + generateLegendStyle(legendItem.getKey()) + "}";
    }

    private String generateLegendStyle(final Map<String, String> style) {
        return "{" + style.entrySet().stream()
                .map(entry -> "'" + entry.getKey() + "': '" + entry.getValue() + "'")
                .collect(Collectors.joining(",")) + "}";
    }

    private String generateValueAccessor(Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(entityType, propertyName);
        if (Money.class.isAssignableFrom(propertyType)) {
            return "this._moneyPropAccessor('" + propertyName + "')";
        } else if (EntityUtils.isDate(propertyType)) {
            final Optional<String> timeZone = ofNullable(getTimeZone(entityType, propertyName));
            final Optional<String> timePortion = ofNullable(getTimePortionToDisplay(entityType, propertyName));
            final String typeSpec = "Date:" + timeZone.orElse(":") + timePortion.orElse("");
            return "this._datePropAccessor('" + propertyName + "', '" + typeSpec + "')";
        }
        return "'" + propertyName + "'";
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
