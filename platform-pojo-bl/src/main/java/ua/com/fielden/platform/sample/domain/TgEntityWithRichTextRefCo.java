package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link TgEntityWithRichTextRef}.
 *
 * @author TG Team
 */
public interface TgEntityWithRichTextRefCo extends IEntityDao<TgEntityWithRichTextRef> {

    static final IFetchProvider<TgEntityWithRichTextRef> FETCH_PROVIDER = EntityUtils.fetch(TgEntityWithRichTextRef.class).with(
            "key", "desc", "richTextRef");
}
