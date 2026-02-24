package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;

import java.util.List;
import java.util.function.Function;

/// An interface for Entity Centre processing API.
///
public interface EntityCentreProcessor {

    /// Executes named Entity Centre configuration, defined by UUID, similarly as the owner may have run it through Web UI.
    /// Takes into account all unsaved changes in that configuration.
    ///
    /// Important: running of this method in context of `@SessionRequired` scope may roll back active transaction.
    /// This means that API users must exercise caution if [Left] is returned (use [Either#orElseThrow(Function)]).
    ///
    <T extends AbstractEntity<?>> Either<Result, List<T>> getResult(
        String configUuid
    );

    /// Finds out whether named Entity Centre configuration, defined by UUID, has non-empty result (similarly to Web UI running).
    /// Takes into account all unsaved changes in that configuration.
    ///
    /// Important: running of this method in context of `@SessionRequired` scope may roll back active transaction.
    /// This means that API users must exercise caution if [Left] is returned (use [Either#orElseThrow(Function)]).
    ///
    Either<Result, Boolean> resultExists(
        String configUuid
    );

}
