package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.utils.Pair;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.user.UserAndRoleAssociation.USER_ROLE;

/// Synthetic entity providing richer search capabilities for entity [User].
///
@CompanionObject(ReUserCo.class)
@EntityTitle(value = "User Ext", desc = "Synthetic entity based on User, which provides additional search capabilities (e.g., search by user roles).")
public class ReUser extends User {

    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(ReUser.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public static final String USER_ROLES = "userRoles";

    protected static final EntityResultQueryModel<ReUser> model_ =
            select(User.class).where()
            .critCondition(select(UserAndRoleAssociation.class).where().prop(UserAndRoleAssociation.USER).eq().extProp(ID), USER_ROLE + "." + KEY, USER_ROLES)
            .yieldAll()
            .modelAsEntity(ReUser.class);


    @IsProperty
    @CritOnly(Type.MULTI)
    @Title(value = "Roles", desc = "User roles to which users may belong (active and inactive).")
    private UserRole userRoles;

    @Observable
    public ReUser setUserRoles(final UserRole userRoles) {
        this.userRoles = userRoles;
        return this;
    }

    public UserRole getUserRoles() {
        return userRoles;
    }

}
