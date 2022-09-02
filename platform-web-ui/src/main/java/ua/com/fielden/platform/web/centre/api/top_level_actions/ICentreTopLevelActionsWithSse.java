package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.rx.IObservableKind;

/**
 * A contract to specify Sever-Side Eventing URI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActionsWithSse<T extends AbstractEntity<?>> extends ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> {

	/**
	 * Accepts URI that is used at the server side to connect to the associated with it event source.
	 * Only non-empty and non-null value is acceptable.
	 *
	 * @param uri
	 * @return
	 */
    ICentreSseWithPromptRefresh<T> hasEventSourceAt(final String uri, final Class<? extends IObservableKind<?>> observableClass);

}
