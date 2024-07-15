package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.sse.IEventSource;

/**
 * A contract to specify Sever-Side Event Source.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActionsWithSse<T extends AbstractEntity<?>> extends ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> {

	/**
	 * Accepts event source class to send event to all registered client via common URI.
	 * Only non-empty and non-null value is acceptable.
	 *
	 * @param eventSourceClass
	 * @return
	 */
    ICentreSseWithPromptRefresh<T> hasEventSource(final Class<? extends IEventSource> eventSourceClass);

}
