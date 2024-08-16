package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.*;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.*;

/**
 * Implementation and the entry point for dynamic column building API. It is used to define dynamic columns that are used for representing collectional properties in-line with the main entity.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class DynamicColumnBuilder<T extends AbstractEntity<?>> implements IDynamicColumnBuilderAddProp, IDynamicColumnBuilderGroupProp, IDynamicColumnBuilderDisplayProp, IDynamicColumnBuilderWithTooltipProp, IDynamicColumnConfig {

    private final List<DynamicColumn<T>> dynamicColumns = new ArrayList<>();
    private final Class<T> type;

    private final String collectionalPropertyName;
    private String groupProp;
    private String displayProp;
    private Optional<String> tooltipProp = Optional.empty();

    /**
     * This is the entry to the Dynamic Column Builder API.
     * @param type
     * @param collectionalPropertyName
     * @return
     */
    public static <M extends AbstractEntity<?>> IDynamicColumnBuilderGroupProp forProperty(final Class<M> type, final CharSequence collectionalPropertyName) {
        return new DynamicColumnBuilder<M>(type, collectionalPropertyName.toString());
    }

    private DynamicColumnBuilder(final Class<T> type, final String collectionalPropertyName) {
        this.type = type;
        this.collectionalPropertyName = collectionalPropertyName;
    }

    @Override
    public IDynamicColumnBuilderWithTitle addColumn(final String groupPropValue) {
        final DynamicColumn<T> column = new DynamicColumn<>(this, groupPropValue);
        dynamicColumns.add(column);
        return column;
    }

    @Override
    public List<Map<String, Object>> build() {
        final Class<?> collectionalPropertyType = PropertyTypeDeterminator.determinePropertyType(type, collectionalPropertyName);
        final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(collectionalPropertyType, displayProp);
        final String type = EntityCentre.egiRepresentationFor(
                propertyType,
                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimeZone(collectionalPropertyType, displayProp) : null),
                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimePortionToDisplay(collectionalPropertyType, displayProp) : null));

        return dynamicColumns.stream().map(dynamicProp -> {
            final Map<String, Object> res = new HashMap<>();
            res.put(DYN_COL_GROUP_PROP_VALUE, dynamicProp.getGroupPropValue());
            res.put(DYN_COL_TYPE, type);
            res.put(DYN_COL_GROUP_PROP, groupProp);
            res.put(DYN_COL_DISPLAY_PROP, displayProp);
            res.put(DYN_COL_TOOLTIP_PROP, tooltipProp.orElse(""));
            res.put(DYN_COL_TITLE, dynamicProp.getTitle());
            res.put(DYN_COL_DESC, dynamicProp.getDesc().orElse(dynamicProp.getTitle()));
            res.put(DYN_COL_WIDTH, dynamicProp.getWidth());
            res.put(DYN_COL_MIN_WIDTH, dynamicProp.getMinWidth());
            res.put(DYN_COL_GROW_FACTOR, dynamicProp.getGrowFactor());
            return res;
        }).collect(toList());
    }

    @Override
    public IDynamicColumnBuilderAddPropWithDone withTooltipProp(final CharSequence tooltipProp) {
        this.tooltipProp = Optional.of(tooltipProp.toString());
        return this;
    }

    @Override
    public IDynamicColumnBuilderWithTooltipProp withDisplayProp(final CharSequence displayProp) {
        this.displayProp = displayProp.toString();
        return this;
    }

    @Override
    public IDynamicColumnBuilderDisplayProp withGroupProp(final CharSequence groupProp) {
        this.groupProp = groupProp.toString();
        return this;
    }

    @Override
    public List<DynamicColumnForExport> buildToExport() {
        return dynamicColumns.stream().map(dynamicProp -> {
            return new DynamicColumnForExport()
                    .setCollectionalPropertyName(collectionalPropertyName)
                    .setGroupProp(groupProp)
                    .setGroupPropValue(dynamicProp.getGroupPropValue())
                    .setTitle(dynamicProp.getTitle())
                    .setDisplayProp(displayProp);
        }).collect(toList());
    }

    @Override
    public IDynamicColumnConfig done() {
        return this;
    }
}
