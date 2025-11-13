package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.types.Hyperlink;

import java.util.Map;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface TinyHyperlinkCo extends IEntityDao<TinyHyperlink> {

    IFetchProvider<TinyHyperlink> FETCH_PROVIDER = fetch(TinyHyperlink.class).with(TinyHyperlink.HASH);

    TinyHyperlink save(Class<? extends AbstractEntity<?>> entityType,
                       Map<String, Object> modifiedProperties,
                       CentreContextHolder centreContextHolder,
                       String actionIdentifier);

    TinyHyperlink save(Class<? extends AbstractEntity<?>> entityType,
                       SavingInfoHolder savingInfoHolder,
                       String actionIdentifier);

    /// Saves a tiny hyperlink whose [TinyHyperlink#target] is `hyperlink`.
    ///
    TinyHyperlink saveWithTarget(Hyperlink hyperlink);

    String toURL(TinyHyperlink tinyHyperlink);

    /// Computes a hash for `tinyHyperlink`.
    /// If it is persisted, returns the existing hash value.
    ///
    String hash(TinyHyperlink tinyHyperlink);

}
