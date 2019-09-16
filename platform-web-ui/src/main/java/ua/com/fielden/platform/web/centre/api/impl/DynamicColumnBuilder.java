package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.valueOf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicPropForExport;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderAddProp;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderGroupProp;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderDisplayProp;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderWithTitle;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderWithTooltipProp;

/**
 * Implementation and the entry point for dynamic column building API. It is used to define dynamic columns that are used for representing collectional properties in-line with the main entity.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class DynamicColumnBuilder<T extends AbstractEntity<?>> implements IDynamicColumnBuilderAddProp, IDynamicColumnBuilderGroupProp, IDynamicColumnBuilderDisplayProp, IDynamicColumnBuilderWithTooltipProp, IDynamicColumnConfig {

    private final List<DynamicColumn<T>> dynamicProps = new ArrayList<>();
    private final Class<T> type;

    private final String collectionalPropertyName;
    private String keyProp;
    private String valueProp;
    private Optional<String> tooltipProp = Optional.empty();

    /**
     * 
     * @param type
     * @param collectionalPropertyName
     * @return
     */
    public static <M extends AbstractEntity<?>> IDynamicColumnBuilderGroupProp forProperty(final Class<M> type, final String collectionalPropertyName) {
        return new DynamicColumnBuilder<M>(type, collectionalPropertyName);
    }

    private DynamicColumnBuilder(final Class<T> type, final String collectionalPropertyName) {
        this.type = type;
        this.collectionalPropertyName = collectionalPropertyName;
    }

    @Override
    public IDynamicColumnBuilderWithTitle addColumn(final String keyPropValue) {
        final DynamicColumn<T> prop = new DynamicColumn<>(this, keyPropValue);
        dynamicProps.add(prop);
        return prop;
    }

    @Override
    public List<Map<String, String>> build() {
        final Class<?> collectionalPropertyType = PropertyTypeDeterminator.determinePropertyType(type, collectionalPropertyName);
        final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(collectionalPropertyType, valueProp);
        final String type = EntityCentre.egiRepresentationFor(
                propertyType,
                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimeZone(collectionalPropertyType, valueProp) : null),
                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimePortionToDisplay(collectionalPropertyType, valueProp) : null));

        return dynamicProps.stream().map(dynamicProp -> {
            final Map<String, String> res = new HashMap<>();
            res.put("keyPropValue", dynamicProp.getGroupPropValue());
            res.put("type", type);
            res.put("keyProp", keyProp);
            res.put("valueProp", valueProp);
            res.put("tooltipProp", tooltipProp.orElse(""));
            res.put("title", dynamicProp.getTitle());
            res.put("desc", dynamicProp.getDesc().orElse(dynamicProp.getTitle()));
            res.put("width", valueOf(dynamicProp.getWidth()));
            res.put("minWidth", valueOf(dynamicProp.getMinWidth()));
            res.put("growFactor", valueOf(dynamicProp.getGrowFactor()));
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public IDynamicColumnBuilderAddProp withTooltipProp(final String tooltipProp) {
        this.tooltipProp = Optional.of(tooltipProp);
        return this;
    }

    @Override
    public IDynamicColumnBuilderWithTooltipProp withDisplayProp(final String valueProp) {
        this.valueProp = valueProp;
        return this;
    }

    @Override
    public IDynamicColumnBuilderDisplayProp withGroupProp(final String keyProp) {
        this.keyProp = keyProp;
        return this;
    }

    @Override
    public List<DynamicPropForExport> buildToExport() {
        return dynamicProps.stream().map(dynamicProp -> {
            return new DynamicPropForExport()
                    .setCollectionalPropertyName(collectionalPropertyName)
                    .setKeyProp(keyProp)
                    .setKeyPropValue(dynamicProp.getGroupPropValue())
                    .setTitle(dynamicProp.getTitle())
                    .setValueProp(valueProp);
        }).collect(Collectors.toList());
    }

    @Override
    public IDynamicColumnConfig done() {
        return this;
    }
}
