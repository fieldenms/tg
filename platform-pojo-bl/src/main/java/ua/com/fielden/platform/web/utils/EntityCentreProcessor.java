package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.List;
import java.util.function.Function;

/// An interface for Entity Centre processing API.
///
/// The intended main usage is to execute named Entity Centre configurations on behalf of their owners as part of business logic.
///
/// Configuration to be executed may belong to [DeviceProfile#DESKTOP] or [DeviceProfile#MOBILE] namespaces.
/// It should correspond to some standalone Entity Centre on module menus of the web application.
/// Embedded Centres are not currently supported due to the need to specify their context through some additional API.
///
/// The next extension to this API may include:
/// - create custom named configurations;
/// - modify existing named configurations with custom criteria values, ordering, page capacity etc.
/// - cover operations for default configurations
/// - cover operations for embedded Entity Centres with some master context (see [IQueryEnhancer]).
///
public interface EntityCentreProcessor {

    /// Executes named Entity Centre configuration, defined by UUID, similarly as the owner may have run it through Web UI.
    /// Takes into account all unsaved changes in that configuration.
    ///
    /// Returns [Left] with invalid [Result] for the cases where
    /// - UUID is not valid
    /// - there is no configuration with that UUID (or there are multiple ones for some reason)
    /// - there are only "orphan" inherited-from-shared configurations with no original one (i.e. if it was deleted)
    /// - UUID represents so-called "link" configuration that originates from parameters like "?poCrit=PO001"
    /// - UUID represents configuration with validation errors (e.g. requiredness or others)
    /// - UUID represents configuration with authorisation errors (either Can Read or Can Read Property for non-empty criterion)
    /// - UUID represents configuration with generator errors.
    ///
    /// Returns [Right] with [List] of entities corresponding to configuration criteria, ordering and page capacity.
    /// Entity Centre with [IGenerator] performs generation of new data during execution through this API.
    ///
    /// Entity Centres with dynamic properties are supported (see `IResultSetBuilderDynamicProps#addProps` API).
    ///
    /// Example:
    /// ```
    /// final List<WorkOrder> workOrders = entityCentreProcessor
    ///     .getResult(configUuid)
    ///     .orElseThrow(Result::throwRuntime);
    /// ```
    ///
    /// **Important**: running of this method in context of `@SessionRequired` scope may roll back active transaction.
    /// This means that API users must exercise caution if [Left] is returned (use [Either#orElseThrow(Function)]).
    ///
    /// @param <T> corresponds to the root entity type of the executed Entity Centre configuration.
    ///
    <T extends AbstractEntity<?>> Either<Result, List<T>> getResult(
        String configUuid
    );

    /// Finds out whether named Entity Centre configuration, defined by UUID, has non-empty result (similarly to Web UI running).
    /// Takes into account all unsaved changes in that configuration.
    ///
    /// Returns [Left] with invalid [Result] for the cases where
    /// - UUID is not valid
    /// - there is no configuration with that UUID (or there are multiple ones for some reason)
    /// - there are only "orphan" inherited-from-shared configurations with no original one (i.e. if it was deleted)
    /// - UUID represents so-called "link" configuration that originates from parameters like "?poCrit=PO001"
    /// - UUID represents configuration with validation errors (e.g. requiredness or others)
    /// - UUID represents configuration with authorisation errors (either Can Read or Can Read Property for non-empty criterion)
    /// - UUID represents configuration with generator errors.
    ///
    /// Returns [Right] with [Boolean] indicator for presence of entities corresponding to configuration criteria.
    /// Entity Centre with [IGenerator] still performs fresh data generation during execution through this API.
    ///
    /// Example:
    /// ```
    /// final boolean workOrdersPresent = entityCentreProcessor
    ///     .resultExists(configUuid)
    ///     .orElseThrow(Result::throwRuntime);
    /// ```
    ///
    /// **Important**: running of this method in context of `@SessionRequired` scope may roll back active transaction.
    /// This means that API users must exercise caution if [Left] is returned (use [Either#orElseThrow(Function)]).
    ///
    Either<Result, Boolean> resultExists(
        String configUuid
    );

}
