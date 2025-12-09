package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.*;
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

/**
 * Represents the entity master for scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public class ScatterPlotMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    private final EntityActionConfig action;
    /**
     * Create new scatter plot according to given configuration
     *
     * @param scatterPlotMasterBuilder
     */
    public ScatterPlotMaster(final ScatterPlotMasterBuilder<T> scatterPlotMasterBuilder) {
        this.action = scatterPlotMasterBuilder.getAction();

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        final Optional<Pair<String, DomElement>> actionPair = generateAction(importPaths, scatterPlotMasterBuilder.getAction());

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/scatter-plot/tg-scatter-plot-master-template.js")
                .replace(IMPORTS, createImports(importPaths))
                .replace(ENTITY_TYPE, flattenedNameOf(scatterPlotMasterBuilder.getEntityType()))
                .replace("<!--@tg-entity-master-content-->", actionPair.map(a -> a.getValue().toString()).orElse(""))
                .replace("//generatedPrimaryActions", actionPair.map(a -> a.getKey().toString()).orElse(""))
                .replace("//@ready-callback", readyCallback(scatterPlotMasterBuilder))
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", String.valueOf(scatterPlotMasterBuilder.shouldSaveOnActivation()));

        this.renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private Optional<Pair<String, DomElement>> generateAction(final LinkedHashSet<String> importPaths, final EntityActionConfig action) {
        if (action != null) {
            final FunctionalActionElement el = FunctionalActionElement.newEntityActionForMaster(action, 0);
            importPaths.add(el.importPath());
            return of(new Pair<>(el.createActionObject(), el.render().clazz("chart-action").attr("hidden", true)));
        }
        return empty();
    }

    private String readyCallback(final ScatterPlotMasterBuilder<T> chartBuilder) {
        //Generate legend
        final List<String> legendItems = new ArrayList<>();
        chartBuilder.getLegend().forEach(item -> {
            legendItems.add(generateLegendItem(item));
        });
        //Generate options
        final String chartOptions = "{\n"
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

        return "self.uuid = self.centreUuid;\n"
                + "self.classList.remove('canLeave');\n"
                + "self._focusFirstInput = function () {};\n"
                + "self.options = " + chartOptions + ";\n"
                + "self.legendItems =[" + join(legendItems, ",\n") + "];\n"
                + "self.categoryRangeSource = '" + chartBuilder.getCategroyRangeConfig().getSource() + "';\n"
                + "self.valueRangeSource = '" + chartBuilder.getValueRangeConfig().getSource() + "';\n"
                + format("self.margins = {top: %s, right: %s, bottom: %s, left: %s};\n", chartBuilder.getTopMargin(), chartBuilder.getRightMargin(),
                        chartBuilder.getBottomMargin(), chartBuilder.getLeftMargin());
    }

    private String getYAxisTitle(final ScatterPlotMasterBuilder<T> chartBuilder) {
        return ofNullable(chartBuilder.getRangeAxisTitle())
                .orElse(getTitleAndDesc(chartBuilder.getCategoryPropertyName(), chartBuilder.getChartEntityType()).getKey());
    }

    private String getXAxisTitle(final ScatterPlotMasterBuilder<T> chartBuilder) {
        return ofNullable(chartBuilder.getDomainAxisTitle())
                .orElse(getTitleAndDesc(chartBuilder.getValuePropertyName(), chartBuilder.getChartEntityType()).getKey());
    }

    private String getChartTitle(final ScatterPlotMasterBuilder<T> chartBuilder) {
        return ofNullable(chartBuilder.getTitle())
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
    public Stream<EntityActionConfig> streamActionConfigs() {
        return Stream.of(action);
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        if (PRIMARY_RESULT_SET.equals(actionKind) && actionNumber == 0) {
            return this.action;
        }
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return empty();
    }

}
