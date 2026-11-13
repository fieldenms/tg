package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

/// Instantiates enhanced entity types stored as [UserDefinedEnhancedEntityType] records.
///
public interface IEntityTypeEnhancement {

    /// Returns a Java class that represents `instance`, with its [UserDefinedCalculatedProperty] records as calculated properties,
    /// and returns it as part of an enhanced entity type.
    /// If `instance` has no such records, the Java class is its base type.
    ///
    /// `instance` must be persisted, and may be uninstrumented.
    ///
    /// `instance` and all its properties are validated, including whether the current user, who must be equal to [UserDefinedEnhancedEntityType#user],
    /// may read all properties that the expressions refer to.
    ///
    /// @throws Result           if the name of `instance` or of any of its properties is invalid
    /// @throws WebApiException  if `instance` or any of its properties is invalid for any other reason
    ///
    EnhancedEntityType javaTypeFor(UserDefinedEnhancedEntityType instance);

}
