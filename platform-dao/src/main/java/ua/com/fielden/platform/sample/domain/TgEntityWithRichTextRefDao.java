package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.TgEntityWithRichTextRef_CanSave_Token;

/**
 * DAO implementation for companion object {@link TgEntityWithRichTextRefCo}.
 *
 * @author TG Team
 */
@EntityType(TgEntityWithRichTextRef.class)
public class TgEntityWithRichTextRefDao extends CommonEntityDao<TgEntityWithRichTextRef> implements TgEntityWithRichTextRefCo {

    @Inject
    public TgEntityWithRichTextRefDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgEntityWithRichTextRef_CanSave_Token.class)
    public TgEntityWithRichTextRef save(final TgEntityWithRichTextRef entity) {
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<TgEntityWithRichTextRef> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
