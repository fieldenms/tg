package ua.com.fielden.platform.entity.validation.test_entities;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractAfterChangeEventHandler;

public class EntityWithDynamicRequirednessEitherOrDefiner extends AbstractAfterChangeEventHandler<Boolean> {
    private final String ERR_NONE_SELECTED = format("Either %s, %s or %s needs to be selected.",
                                                         getTitleAndDesc("prop6", EntityWithDynamicRequiredness.class).getKey(),
                                                         getTitleAndDesc("prop7", EntityWithDynamicRequiredness.class).getKey(),
                                                         getTitleAndDesc("prop8", EntityWithDynamicRequiredness.class).getKey());
    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean value) {
        final EntityWithDynamicRequiredness entity = property.getEntity();
        final MetaProperty<Boolean> mpProp6 = entity.getProperty("prop6");
        final MetaProperty<Boolean> mpProp7 = entity.getProperty("prop7");
        final MetaProperty<Boolean> mpProp8 = entity.getProperty("prop8");
        property.setRequired(value, ERR_NONE_SELECTED);
        if (!entity.isInitialising() && value) {
            if ("prop6".equals(property.getName())) {
                mpProp7.setRequired(false);
                mpProp8.setRequired(false);
                entity.setProp7(false);
                entity.setProp8(false);
            } else if ("prop7".equals(property.getName())) {
                mpProp6.setRequired(false);
                mpProp8.setRequired(false);
                entity.setProp6(false);
                entity.setProp8(false);
            } else if ("prop8".equals(property.getName())) {
                mpProp6.setRequired(false);
                mpProp7.setRequired(false);
                entity.setProp6(false);
                entity.setProp7(false);
            }
        }
    }

}