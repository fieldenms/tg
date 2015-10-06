package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify Sever-Side Eventing URI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActionsWithSse<T extends AbstractEntity<?>> extends ICentreTopLevelActions<T> {

	/**
	 * Accepts URI that is used at the server side to connect to the associated with it event source.
	 * Only non-empty and non-null value is acceptable.
	 * 
	 * @param uri
	 * @return
	 */
    ICentreTopLevelActions<T> hasEventSourceAt(final String uri);

}
