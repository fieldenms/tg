package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link TgEntityWithRichTextProp}.
 *
 * @author TG Team
 */
public interface TgEntityWithRichTextPropCo extends IEntityDao<TgEntityWithRichTextProp> {

    static final IFetchProvider<TgEntityWithRichTextProp> FETCH_PROVIDER = EntityUtils.fetch(TgEntityWithRichTextProp.class).with(
            // TODO uncomment the following line and specify properties required for UI. Then remove the line after it.
            // MetaModels.TgEntityWithRichTextProp_.key(), MetaModels.TgEntityWithRichTextProp_.desc());
            "Please specify properties required for UI");

}
