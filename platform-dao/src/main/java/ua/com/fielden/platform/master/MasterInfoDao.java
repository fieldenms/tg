package ua.com.fielden.platform.master;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(MasterInfo.class)
public class MasterInfoDao extends CommonEntityDao<MasterInfo> implements IMasterInfo {

    @Inject
    public MasterInfoDao(final IFilter filter) {
        super(filter);
    }
}
