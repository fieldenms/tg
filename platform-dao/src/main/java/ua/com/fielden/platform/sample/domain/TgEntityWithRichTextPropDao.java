package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.TgEntityWithRichTextProp_CanSave_Token;

/**
 * DAO implementation for companion object {@link TgEntityWithRichTextPropCo}.
 *
 * @author TG Team
 */
@EntityType(TgEntityWithRichTextProp.class)
public class TgEntityWithRichTextPropDao extends CommonEntityDao<TgEntityWithRichTextProp> implements TgEntityWithRichTextPropCo {

    @Inject
    public TgEntityWithRichTextPropDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgEntityWithRichTextProp_CanSave_Token.class)
    public TgEntityWithRichTextProp save(final TgEntityWithRichTextProp entity) {
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<TgEntityWithRichTextProp> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
