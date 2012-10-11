package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.util.EventListener;

/**
 * Listens the "lifecycle model update" event.
 *
 * @author TG Team
 *
 */
public interface ILifecycleModelUpdated extends EventListener {

    void lifecycleModelUpdated(final LifecycleModelUpdateEvent<?> event);
}
