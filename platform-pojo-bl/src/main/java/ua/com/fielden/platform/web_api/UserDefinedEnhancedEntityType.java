package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeBaseTypeValidator;
import ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeNameValidator;

/// An enhanced entity type defined by a user, as stored in the database.
///
/// Its calculated properties are stored as [UserDefinedCalculatedProperty] records, and [IEntityTypeEnhancement#javaTypeFor] instantiates it as a Java class.
///
@EntityTitle(value = "User-defined Enhanced Entity Type", desc = "An entity type that a user defined by extending a domain entity type with calculated properties.")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(UserDefinedEnhancedEntityTypeCo.class)
public class UserDefinedEnhancedEntityType extends AbstractPersistentEntity<DynamicEntityKey> {

    public static final String
            NAME = "name",
            USER = "user",
            BASE_TYPE = "baseType";

    @IsProperty(length = 1024)
    @MapTo
    @CompositeKeyMember(1)
    @Title(value = "Name", desc = "The name by which MCP clients refer to this entity type, unrelated to the name of its Java class.")
    @BeforeChange(@Handler(EnhancedEntityTypeNameValidator.class))
    private String name;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    @Title(value = "User", desc = "The user who defined this entity type.")
    private User user;

    @IsProperty(length = 1024)
    @MapTo
    @Required
    @Title(value = "Base Type", desc = "The fully-qualified name of the domain entity type that this entity type extends.")
    @BeforeChange(@Handler(EnhancedEntityTypeBaseTypeValidator.class))
    private String baseType;

    public String getName() {
        return name;
    }

    @Observable
    public UserDefinedEnhancedEntityType setName(final String name) {
        this.name = name;
        return this;
    }

    public User getUser() {
        return user;
    }

    @Observable
    public UserDefinedEnhancedEntityType setUser(final User user) {
        this.user = user;
        return this;
    }

    public String getBaseType() {
        return baseType;
    }

    @Observable
    public UserDefinedEnhancedEntityType setBaseType(final String baseType) {
        this.baseType = baseType;
        return this;
    }

}
