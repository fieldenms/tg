package ua.com.fielden.platform.master;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IMasterInfoProvider {

    MasterInfo getMasterInfo(Class<? extends AbstractEntity<?>> type);
}
