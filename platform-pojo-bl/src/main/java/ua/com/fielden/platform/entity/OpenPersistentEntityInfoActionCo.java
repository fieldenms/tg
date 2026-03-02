package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

/// Companion object for the {@link OpenPersistentEntityInfoAction} entity.
///
public interface OpenPersistentEntityInfoActionCo extends IEntityDao<OpenPersistentEntityInfoAction> {

    IFetchProvider<OpenPersistentEntityInfoAction> FETCH_PROVIDER = fetch(OpenPersistentEntityInfoAction.class).with(
            // key is needed to be correctly autopopulated by newly saved compound master entity (ID-based restoration of entity-typed key)
            "key");

}
