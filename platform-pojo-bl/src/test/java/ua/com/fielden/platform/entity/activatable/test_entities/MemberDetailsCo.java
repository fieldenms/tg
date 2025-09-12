package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;


public interface MemberDetailsCo extends IEntityDao<MemberDetails> {

    IFetchProvider<MemberDetails> FETCH_PROVIDER = fetch(MemberDetails.class).with("union");

}
