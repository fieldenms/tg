package ua.com.fielden.platform.entity.meta.impl;

import com.google.inject.Inject;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.utils.IDates;

/**
 * A convenient base class for implementing {@link IAfterChangeEventHandler} that would benefit from easy access to various companion objects as entity readers.
 * It is envisaged that all ACE handlers are instantiated by means of IoC, which is required to properly initialise instances of {@link AbstractAfterChangeEventHandler}.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractAfterChangeEventHandler<T> implements IAfterChangeEventHandler<T> {

    @Inject
    private ICompanionObjectFinder coFinder;
    @Inject
    private IDates dates;

    /**
     * Returns an instance of a companion object for entity of {@code type} as {@link IEntityReader}.
     *
     * @param type
     * @return
     */
    protected <R extends IEntityReader<E>, E extends AbstractEntity<?>> R co(final Class<E> type) {
        return coFinder.findAsReader(type, true);
    }

    /**
     * Returns {@link ua.com.fielden.platform.utils.IDates} instance to provide access to dates API in definers (post-conditions).
     *
     * @return
     */
    protected IDates dates() {
        return dates;
    }

}
