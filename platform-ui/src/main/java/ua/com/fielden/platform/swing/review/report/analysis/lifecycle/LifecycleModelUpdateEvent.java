package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;

/**
 * The {@link EventObject} that represents the "lifecycle model update" event.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class LifecycleModelUpdateEvent<T extends AbstractEntity<?>> extends EventObject {

    private static final long serialVersionUID = 6625913606901949632L;

    private final LifecycleModel<T> lifecycleModel;

    /**
     * Initiates this event with {@link LifecycleAnalysisModel} and  {@link LifecycleModel} instances.
     *
     * @param source
     * @param lifecycleModel
     */
    public LifecycleModelUpdateEvent(final LifecycleAnalysisModel<T> source, final LifecycleModel<T> lifecycleModel) {
	super(source);
	this.lifecycleModel = lifecycleModel;
    }

    public LifecycleModel<T> getLifecycleModel() {
	return lifecycleModel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public LifecycleAnalysisModel<T> getSource() {
        return (LifecycleAnalysisModel<T>)super.getSource();
    }
}
