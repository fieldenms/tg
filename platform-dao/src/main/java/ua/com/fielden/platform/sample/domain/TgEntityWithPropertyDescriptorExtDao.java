package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgEntityWithPropertyDescriptorExtCo}.
 *
 * @author TG Team
 */
@EntityType(TgEntityWithPropertyDescriptorExt.class)
public class TgEntityWithPropertyDescriptorExtDao extends CommonEntityDao<TgEntityWithPropertyDescriptorExt> implements TgEntityWithPropertyDescriptorExtCo {

    @Override
    protected IFetchProvider<TgEntityWithPropertyDescriptorExt> createFetchProvider() {
        return super.createFetchProvider().with("propertyDescriptorSingleCrit");
    }

}
