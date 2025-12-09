package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.utils.Pair;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// A synthetic entity that represents active associations between [User] and [UserRole] entities.
///
@KeyType(DynamicEntityKey.class)
@CompanionObject(SynUserAndRoleAssociationActiveCo.class)
public class SynUserAndRoleAssociationActive extends UserAndRoleAssociation {
    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(SynUserAndRoleAssociationActive.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    protected static final EntityResultQueryModel<UserAndRoleAssociation> model_ = select(UserAndRoleAssociation.class).where().prop(ACTIVE).eq().val(true).model();

}