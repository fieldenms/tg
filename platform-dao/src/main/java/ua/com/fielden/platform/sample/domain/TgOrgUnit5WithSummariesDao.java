package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgOrgUnit5WithSummaries.class)
public class TgOrgUnit5WithSummariesDao extends CommonEntityDao<TgOrgUnit5WithSummaries> implements TgOrgUnit5WithSummariesCo {
}
