package ua.com.fielden.platform.types;

import ua.com.fielden.platform.error.Result;

/**
 * A contract, intended to be implemented first of all by value types that perform self-validation upon instantiation of values.
 */
public interface IWithValidation {

    Result isValid();

}
