package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/// Open Action for Compound {@link PersistentEntityInfo} master.
///
@KeyType(PersistentEntityInfo.class)
@EntityTitle("Persistent Entity Version Info Master")
@CompanionObject(OpenPersistentEntityInfoActionCo.class)
public class OpenPersistentEntityInfoAction extends AbstractFunctionalEntityToOpenCompoundMaster<PersistentEntityInfo> {

    public static final String MAIN = "Main";
    public static final String AUDIT = "Audit";

}