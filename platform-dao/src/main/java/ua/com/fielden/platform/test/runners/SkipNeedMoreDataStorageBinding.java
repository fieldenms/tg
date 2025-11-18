package ua.com.fielden.platform.test.runners;

import ua.com.fielden.platform.continuation.NeedMoreDataStorage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// An annotation for tests to skip their exeuction in a context of a bound [NeedMoreDataStorage].
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SkipNeedMoreDataStorageBinding {
}
