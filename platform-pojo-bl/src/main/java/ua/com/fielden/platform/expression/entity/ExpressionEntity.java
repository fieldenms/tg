package ua.com.fielden.platform.expression.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty;
import ua.com.fielden.platform.utils.ClassComparator;

/**
 * This entity holds the information that allows one to create instance of {@link ICalculatedProperty} class.
 * It holds:
 * <dl>
 * <dt> <i>Name</i>
 * <dd> represents the name of new calculated property.
 * <dt> <i>Title</i>
 * <dd> represents the title of new calculated property.
 * <dt> <i>Description</i>
 * <dd> represents the description of new calculated property.
 * <dt> <i>Expression</i>
 * <dd> represents the expression that is used to evaluate value for calculated property.
 * </dl>
 * 
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@EntityTitle(value = "Expression", desc = "Expression entity")
@DescTitle(value = "Description", desc= "Property description")
public class ExpressionEntity extends AbstractEntity<DynamicEntityKey> {

    private static final long serialVersionUID = 8052106123237873060L;

    @IsProperty
    @CompositeKeyMember(1)
    @Required
    @Title(value = "Entity class")
    @Invisible
    private Class<? extends AbstractEntity> entityClass;

    @IsProperty
    @CompositeKeyMember(2)
    @Required
    @Title(value = "Name", desc = "Property name")
    private String name;

    @IsProperty
    @Title(value = "Title", desc = "Property title")
    @Required
    private String title;

    @IsProperty
    @Title(value = "Expression", desc = "Property evaluation formula")
    @Required
    private String expression;

    /**
     * Default constructor.
     */
    protected ExpressionEntity(){
	final DynamicEntityKey key = new DynamicEntityKey(this);
	key.addKeyMemberComparator(1, new ClassComparator());
	setKey(key);
    }

    public Class<? extends AbstractEntity> getEntityClass() {
	return entityClass;
    }

    @Observable
    @NotNull
    public void setEntityClass(final Class<? extends AbstractEntity> entityClass) {
	this.entityClass = entityClass;
    }

    public String getName() {
	return name;
    }

    @Observable
    @NotNull
    public void setName(final String name) {
	this.name = name;
    }

    public String getTitle() {
	return title;
    }

    @Observable
    @NotNull
    public void setTitle(final String title) {
	this.title = title;
    }

    public String getExpression() {
	return expression;
    }

    @Observable
    @NotNull
    @DomainValidation
    public void setExpression(final String expression) {
	this.expression = expression;
    }

}
