package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.money.IMoneyConfig0;

/**
 * A configuration for a widget to edit <code>Money</code> properties.
 * <p>
 * In case of HTML a corresponding widget should either be an <code>input</text> with <code>type="number"</code> and <code>step="0.01"</code>, or a custom component.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMoneyConfig<T extends AbstractEntity<?>> extends IMoneyConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    IMoneyConfig0<T> skipValidation();
}
