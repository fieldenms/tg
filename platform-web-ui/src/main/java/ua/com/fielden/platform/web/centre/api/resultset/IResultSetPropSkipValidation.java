package ua.com.fielden.platform.web.centre.api.resultset;

public interface IResultSetPropSkipValidation<T> {

    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped. This should be done for optimisation reasons only in relation to
     * properties that have heavy validation. It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    T skipValidation();
}
