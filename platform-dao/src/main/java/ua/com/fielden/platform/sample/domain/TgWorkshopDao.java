package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgWorkshop.class)
public class TgWorkshopDao extends CommonEntityDao<TgWorkshop> implements ITgWorkshop {

    @SessionRequired
    @Override
    public void delete(final TgWorkshop entity) {
        defaultDelete(entity);
    }

}
