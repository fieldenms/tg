package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.TgNote_CanSave_Token;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * DAO implementation for companion object {@link TgNote}.
 *
 * @author TG Team
 */
@EntityType(TgNote.class)
public class TgNoteDao extends CommonEntityDao<TgNote> implements TgNoteCo {

    @Inject
    public TgNoteDao() {
    }

    @Override
    @SessionRequired
    @Authorise(TgNote_CanSave_Token.class)
    public TgNote save(final TgNote entity) {
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<TgNote> createFetchProvider() {
        return EntityUtils.fetch(TgNote.class).with("key", "text");
    }

}
