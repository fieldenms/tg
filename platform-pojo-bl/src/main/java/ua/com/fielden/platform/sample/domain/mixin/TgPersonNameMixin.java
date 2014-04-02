package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.ITgPersonName;
import com.google.inject.Inject;

/**
 * Mixin implementation for companion object {@link ITgPersonName}.
 * 
 * @author Developers
 * 
 */
public class TgPersonNameMixin {

    private final ITgPersonName companion;

    @Inject
    public TgPersonNameMixin(final ITgPersonName companion) {
        this.companion = companion;
    }

}