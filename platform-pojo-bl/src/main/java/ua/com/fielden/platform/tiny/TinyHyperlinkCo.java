package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.either.Either;

import java.util.Map;
import java.util.Optional;

import static ua.com.fielden.platform.tiny.TinyHyperlink.HASH;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface TinyHyperlinkCo extends IEntityDao<TinyHyperlink> {

    IFetchProvider<TinyHyperlink> FETCH_PROVIDER = fetch(TinyHyperlink.class).with(HASH);

    /// Creates and saves a tiny hyperlink that executes a shared action entity.
    ///
    /// @param entityType
    ///        The entity type associated with the action’s master.
    ///        For standard "open new" actions, this must be a persistent domain entity type (e.g., `Person`).
    ///        For actions that open a master for a functional entity, this must be the functional entity type itself
    ///        (e.g., `CopyWorkOrderAction`).
    /// @param modifiedProperties
    ///        A mapping of property names to values that will be assigned to the constructed instance of `entityType`.
    ///        These assignments take effect as though the user had entered them manually.
    /// @param centreContextHolder
    ///        The context in which the action will be executed.
    /// @param actionIdentifier
    ///        The identifier of the `EntityActionConfig` to use when executing the action.
    ///        The caller must ensure that this identifier corresponds to an existing action configuration.
    ///
    /// @return The saved tiny hyperlink refetched with a default fetch model.
    ///
    TinyHyperlink save(
            Class<? extends AbstractEntity<?>> entityType,
            Map<? extends CharSequence, Object> modifiedProperties,
            CentreContextHolder centreContextHolder,
            String actionIdentifier);

    /// Overloads [#save(Class, Map, CentreContextHolder, String, Optional)].
    ///
    /// @param maybeFetch The fetch model for the returned tiny hyperlink.
    ///
    Either<Long, TinyHyperlink> save(
            Class<? extends AbstractEntity<?>> entityType,
            Map<? extends CharSequence, Object> modifiedProperties,
            CentreContextHolder centreContextHolder,
            String actionIdentifier,
            Optional<fetch<TinyHyperlink>> maybeFetch);

    /// Creates and saves a tiny hyperlink that executes a shared action entity.
    ///
    /// This method is primarily for platform use.
    ///
    TinyHyperlink save(
            Class<? extends AbstractEntity<?>> entityType,
            SavingInfoHolder savingInfoHolder,
            String actionIdentifier);

    /// Overloads [#save(Class, SavingInfoHolder, String)].
    ///
    /// @param maybeFetch The fetch model for the returned tiny hyperlink.
    ///
    Either<Long, TinyHyperlink> save(
            Class<? extends AbstractEntity<?>> entityType,
            SavingInfoHolder savingInfoHolder,
            String actionIdentifier,
            Optional<fetch<TinyHyperlink>> maybeFetch);

    /// Creates and saves a tiny hyperlink whose [TinyHyperlink#target] is `hyperlink`.
    ///
    /// The returned tiny hyperlink is refetched with a default fetch model.
    ///
    TinyHyperlink saveWithTarget(Hyperlink hyperlink);

    /// Creates and saves a tiny hyperlink whose [TinyHyperlink#target] is `hyperlink`.
    ///
    Either<Long, TinyHyperlink> saveWithTarget(Hyperlink hyperlink, Optional<fetch<TinyHyperlink>> maybeFetch);

    /// Converts `tinyHyperlink` into a URL for the current application.
    ///
    String toURL(TinyHyperlink tinyHyperlink);

    /// Computes a hash for `tinyHyperlink`.
    /// If it is persisted, returns the existing hash value.
    ///
    String hash(TinyHyperlink tinyHyperlink);

}
