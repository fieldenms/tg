package ua.com.fielden.platform.web.centre.api.extra_fetch;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;

/**
 *
 * This contract is part of Entity Centre DSL, which provides a way to specify an instance of {@link IFetchProvider} in order to enhance
 * the fetch strategy that gets determined automatically based on the specified result set with additional fetch requirements.
 * This is always an extra fetch information that never reduces the automatically determined one, but only enhances it with more properties to fetch.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IExtraFetchProviderSetter<T extends AbstractEntity<?>> extends IEcbCompletion<T> {

    IEcbCompletion<T> setFetchProvider(final IFetchProvider<T> fetchProvider);
}
