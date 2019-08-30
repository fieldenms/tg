package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.valueOf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IDynamicPropConfig;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderAddProp;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderValueProp;

public class DynamicPropBuilder<T extends AbstractEntity<?>> implements IDynamicPropBuilderAddProp, IDynamicPropConfig {

    private List<DynamicProp<T>> dynamicProps = new ArrayList<>();

    @Override
    public IDynamicPropBuilderValueProp addProp(final String keyProp, final String type) {
        final DynamicProp<T> prop = new DynamicProp<>(this, keyProp, type);
        dynamicProps.add(prop);
        return prop;
    }

    @Override
    public List<Map<String, String>> build() {
        return dynamicProps.stream().map(dynamicProp -> {
            final Map<String, String> res = new HashMap<>();
            res.put("keyProp", dynamicProp.getKeyProp());
            res.put("type", dynamicProp.getType());
            res.put("valueProp", dynamicProp.getValueProp());
            res.put("tooltipProp", dynamicProp.getTooltipProp().orElse(""));
            res.put("title", dynamicProp.getTitle());
            res.put("desc", dynamicProp.getDesc().orElse(dynamicProp.getTitle()));
            res.put("width", valueOf(dynamicProp.getWidth()));
            res.put("minWidth", valueOf(dynamicProp.getMinWidth()));
            return res;
        }).collect(Collectors.toList());
    }

}
