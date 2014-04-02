package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.ITgAuthor;
import com.google.inject.Inject;

/**
 * Mixin implementation for companion object {@link ITgAuthor}.
 * 
 * @author Developers
 * 
 */
public class TgAuthorMixin {

    private final ITgAuthor companion;

    @Inject
    public TgAuthorMixin(final ITgAuthor companion) {
        this.companion = companion;
    }

}