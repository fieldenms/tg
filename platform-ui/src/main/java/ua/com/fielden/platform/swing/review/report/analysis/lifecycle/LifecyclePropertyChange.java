package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class LifecyclePropertyChange implements IAfterChangeEventHandler<LifecyclePropertiesUpdater> {

    @Override
    public void handle(final MetaProperty property, final LifecyclePropertiesUpdater entityPropertyValue) {
	if("from".equals(property.getName())){
	    entityPropertyValue.getLdtm().setFrom(entityPropertyValue.getFrom());
	}else if("to".equals(property.getName())){
	    entityPropertyValue.getLdtm().setTo(entityPropertyValue.getTo());
	}
    }

}
