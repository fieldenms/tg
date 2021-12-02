package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.utils.Pair;

/**
 * Synthetic entity providing reacher search capabilities for entity {@link User}.
 *
 * @author TG Team
 *
 */
@CompanionObject(ReUserCo.class)
@EntityTitle(value = "User Ext", desc = "Synthetic entity based on User, which provides additional search capabilities (e.g., search by user roles).")
public class ReUser extends User {

    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(ReUser.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    protected static final EntityResultQueryModel<ReUser> model_ =
            select(User.class).where()
            .critCondition(select(UserAndRoleAssociation.class).where().prop("user").eq().extProp("id"), "userRole.key", "userRoles")
            .yieldAll()
            .modelAsEntity(ReUser.class);


    @IsProperty
    @CritOnly(Type.MULTI)
    @Title(value = "Roles", desc = "User roles to which users may belong.")
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