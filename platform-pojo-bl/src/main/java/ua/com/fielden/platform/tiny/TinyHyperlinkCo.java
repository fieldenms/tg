package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;

import java.util.Map;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface TinyHyperlinkCo extends IEntityDao<TinyHyperlink> {

    IFetchProvider<TinyHyperlink> FETCH_PROVIDER = fetch(TinyHyperlink.class).with(
            TinyHyperlink.USER,
            TinyHyperlink.CREATED_DATE,
            TinyHyperlink.ENTITY_TYPE_NAME,
            TinyHyperlink.SAVING_INFO_HOLDER);

    TinyHyperlink save(Class<? extends AbstractEntity<?>> entityType,
                       Map<String, Object> modifiedProperties,
                       CentreContextHolder centreContextHolder);

    TinyHyperlink save(Class<? extends AbstractEntity<?>> entityType, SavingInfoHolder savingInfoHolder);

    String toURL(TinyHyperlink tinyHyperlink);

}
