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
import ua.com.fielden.platform.web.centre.api.IDynamicPropConfig;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderAddProp;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderKeyProp;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderValueProp;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWithTitle;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWithTooltipProp;

public class DynamicPropBuilder<T extends AbstractEntity<?>> implements IDynamicPropBuilderAddProp, IDynamicPropBuilderKeyProp, IDynamicPropBuilderValueProp, IDynamicPropBuilderWithTooltipProp, IDynamicPropConfig {

    private final List<DynamicProp<T>> dynamicProps = new ArrayList<>();
    private final Class<T> type;

    private final String collectionalPropertyName;
    private String keyProp;
    private String valueProp;
    private Optional<String> tooltipProp = Optional.empty();

    public static <M extends AbstractEntity<?>> IDynamicPropBuilderKeyProp forProperty(final Class<M> type, final String collectionalPropertyName) {
        return new DynamicPropBuilder<M>(type, collectionalPropertyName);
    }

    private DynamicPropBuilder(final Class<T> type, final String collectionalPropertyName) {
        this.type = type;
        this.collectionalPropertyName = collectionalPropertyName;
    }

    @Override
    public IDynamicPropBuilderWithTitle addProp(final String keyPropValue) {
        final DynamicProp<T> prop = new DynamicProp<>(this, keyPropValue);
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
            res.put("keyPropValue", dynamicProp.getKeyPropValue());
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
    public IDynamicPropBuilderAddProp withTooltipProp(final String tooltipProp) {
        this.tooltipProp = Optional.of(tooltipProp);
        return this;
    }

    @Override
    public IDynamicPropBuilderWithTooltipProp withValueProp(final String valueProp) {
        this.valueProp = valueProp;
        return this;
    }

    @Override
    public IDynamicPropBuilderValueProp withKeyProp(final String keyProp) {
        this.keyProp = keyProp;
        return this;
    }

    @Override
    public List<DynamicPropForExport> buildToExport() {
        return dynamicProps.stream().map(dynamicProp -> {
            return new DynamicPropForExport()
                    .setCollectionalPropertyName(collectionalPropertyName)
                    .setKeyProp(keyProp)
                    .setKeyPropValue(dynamicProp.getKeyPropValue())
                    .setTitle(dynamicProp.getTitle())
                    .setValueProp(valueProp);
        }).collect(Collectors.toList());
    }

    @Override
    public IDynamicPropConfig done() {
        return this;
    }
}
