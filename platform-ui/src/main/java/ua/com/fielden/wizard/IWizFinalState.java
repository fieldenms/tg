package ua.com.fielden.wizard;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A class that represents a single finish state that is associated with a wizard page. A wizard may have several finish states.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IWizFinalState<T extends AbstractEntity<?>> extends IWizState<T> {
    IWizState<T> prev();

    IWizState<T> cancel();

    IWizState<T> finish();
}
