package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.web_api.validators.UserDefinedCalculatedPropertyNameValidator;

/// A calculated property of a [UserDefinedEnhancedEntityType], as stored in the database.
///
/// Whether [#expression] is valid depends on the other properties of the same enhanced entity type, which it may refer to.
/// Therefore, expressions are validated together, when [IEntityTypeEnhancement#javaTypeFor] instantiates the enhanced entity type.
///
@EntityTitle(value = "User-defined Calculated Property", desc = "A calculated property of an entity type that a user defined.")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(UserDefinedCalculatedPropertyCo.class)
public class UserDefinedCalculatedProperty extends AbstractPersistentEntity<DynamicEntityKey> {

    public static final String
            ENTITY_TYPE = "entityType",
            NAME = "name",
            EXPRESSION = "expression";

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Title(value = "Entity Type", desc = "The enhanced entity type that contains this property.")
    private UserDefinedEnhancedEntityType entityType;

    @IsProperty(length = 1024)
    @MapTo
    @CompositeKeyMember(2)
    @Title(value = "Name", desc = "The name of this property.")
    @BeforeChange(@Handler(UserDefinedCalculatedPropertyNameValidator.class))
    private String name;

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @Required
    @Title(value = "Expression", desc = "The expression of this property, in the Expression Language of TG.")
    private String expression;

    public UserDefinedEnhancedEntityType getEntityType() {
        return entityType;
    }

    @Observable
    public UserDefinedCalculatedProperty setEntityType(final UserDefinedEnhancedEntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public UserDefinedCalculatedProperty setName(final String name) {
        this.name = name;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    @Observable
    public UserDefinedCalculatedProperty setExpression(final String expression) {
        this.expression = expression;
        return this;
    }

}
