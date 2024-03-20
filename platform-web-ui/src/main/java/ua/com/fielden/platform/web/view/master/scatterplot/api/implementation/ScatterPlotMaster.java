package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
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
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartLine;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
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
                .replace("@prefDim", "null").replace("@noUiValue", "false")
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
                + "    label: '" + chartBuilder.getTitle() + "',\n"
                + "    xAxis: {\n"
                + "        label: '" + chartBuilder.getXAxisTitle() + "'\n"
                + "    },\n"
                + "    yAxis: {\n"
                + "        label: '" + chartBuilder.getYAxisTitle() + "'\n"
                + "    },\n"
                + "    dataPropertyNames: {\n"
                + "        categoryProp: " + generateValueAccessor(chartBuilder.getEntityType(), chartBuilder.getCategoryPropertyName()) + ",\n"
                + "        valueProp: " + generateValueAccessor(chartBuilder.getEntityType(), chartBuilder.getValuePropertyName()) + ",\n"
                + "        styleProp: '" + chartBuilder.getStylePropertyName() + "',\n"
                + "    },\n"
                + "    tooltip: (d, i) => this._tooltip(d, "
                            + generateValueAccessor(deck.getEntityType(), deck.getPropertyType(), deck.getGroupKeyProp()) + ", "
                            + generateValueAccessor(deck.getEntityType(), String.class, deck.getGroupDescProperty()) + ", "
                            + "self.barOptions[" + deckIndex + "].propertyNames[i], "
                            + "self.barOptions[" + deckIndex + "].propertyTypes[i], "
                            + "self.legendItems[" + deckIndex + "][i].title, " + deckIndex + ", i),\n"
                + "    click: this._click(" + deckIndex + ")\n"
                + "}";

        return "self.barOptions = [" + join(chartOptions, ",\n") + "];\n"
                + "self.legendItems =[" + join(legendItems, ",\n") + "];\n";
    }


    private String generateLegendItem(final Pair<Map<String, String>, String> legendItem) {
        return "{title: '"  + (isEmpty(legendItem.getValue()) ? "" : legendItem.getValue()) + "', style: " + generateLegendStyle(legendItem.getKey()) + "}";
    }

    private String generateLegendStyle(final Map<String, String> style) {
        return "{" + style.entrySet().stream()
                .map(entry -> "'" + entry.getKey() + "': '" + entry.getValue() + "'")
                .collect(Collectors.joining(",")) + "}";
    }

    private String generateValueAccessor(Class<T> entityType, final String propertyName) {
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
