package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(MemberDetails.class)
public class MemberDetailsDao extends CommonEntityDao<MemberDetails> implements MemberDetailsCo {

    @Override
    protected IFetchProvider<MemberDetails> createFetchProvider() {
        return MemberDetailsCo.FETCH_PROVIDER;
    }

}
