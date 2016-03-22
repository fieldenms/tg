package ua.com.fielden.platform.web.view.master.api.helpers;

/**
 * This is an interface to provide skipValidation functionality after property addition.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISkipValidation<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped. This should be done for optimisation reasons only in relation to
     * properties that have heavy validation. It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    T skipValidation();
}
