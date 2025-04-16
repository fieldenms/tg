package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgInventoryIssue.class)
public class TgInventoryIssueDao extends CommonEntityDao<TgInventoryIssue> implements TgInventoryIssueCo {

    @Inject
    protected TgInventoryIssueDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgInventoryIssue> createFetchProvider() {
        return super.createFetchProvider().with("bin", "issueDate", "supersededInventory", "qty");
    }

}
