package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.util.Date;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class LifecyclePropertyChange implements IAfterChangeEventHandler<Date> {

    @Override
    public void handle(final MetaProperty property, final Date entityPropertyValue) {
        if ("from".equals(property.getName())) {
            ((LifecyclePropertiesUpdater) property.getEntity()).getLdtm().setFrom(entityPropertyValue);
        } else if ("to".equals(property.getName())) {
            ((LifecyclePropertiesUpdater) property.getEntity()).getLdtm().setTo(entityPropertyValue);
        }
    }

}
