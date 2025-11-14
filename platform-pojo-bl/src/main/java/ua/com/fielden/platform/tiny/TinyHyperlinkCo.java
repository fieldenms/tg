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

    TinyHyperlink save(
            Class<? extends AbstractEntity<?>> entityType,
            Map<String, Object> modifiedProperties,
            CentreContextHolder centreContextHolder,
            String actionIdentifier);

    Either<Long, TinyHyperlink> save(
            Class<? extends AbstractEntity<?>> entityType,
            Map<String, Object> modifiedProperties,
            CentreContextHolder centreContextHolder,
            String actionIdentifier,
            Optional<fetch<TinyHyperlink>> maybeFetch);

    TinyHyperlink save(
            Class<? extends AbstractEntity<?>> entityType,
            SavingInfoHolder savingInfoHolder,
            String actionIdentifier);

    Either<Long, TinyHyperlink> save(
            Class<? extends AbstractEntity<?>> entityType,
            SavingInfoHolder savingInfoHolder,
            String actionIdentifier,
            Optional<fetch<TinyHyperlink>> maybeFetch);

    /// Saves a tiny hyperlink whose [TinyHyperlink#target] is `hyperlink`.
    ///
    /// The returned tiny hyperlink is refetched with a default fetch model.
    ///
    TinyHyperlink saveWithTarget(Hyperlink hyperlink);

    /// Saves a tiny hyperlink whose [TinyHyperlink#target] is `hyperlink`.
    ///
    Either<Long, TinyHyperlink> saveWithTarget(Hyperlink hyperlink, Optional<fetch<TinyHyperlink>> maybeFetch);

    String toURL(TinyHyperlink tinyHyperlink);

    /// Computes a hash for `tinyHyperlink`.
    /// If it is persisted, returns the existing hash value.
    ///
    String hash(TinyHyperlink tinyHyperlink);

}
