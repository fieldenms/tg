package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.UnionEntityWithoutSecondDescTitleCo;

/**
 * DAO implementation for companion object {@link UnionEntityWithoutSecondDescTitleCo}.
 *
 * @author TG Team
 */
@EntityType(UnionEntityWithoutSecondDescTitle.class)
public class UnionEntityWithoutSecondDescTitleDao extends CommonEntityDao<UnionEntityWithoutSecondDescTitle> implements UnionEntityWithoutSecondDescTitleCo {

}
