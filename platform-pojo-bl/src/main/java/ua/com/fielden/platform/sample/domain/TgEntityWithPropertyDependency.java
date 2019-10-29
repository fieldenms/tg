package ua.com.fielden.platform.sample.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.sample.domain.definers.PropDefiner;
import ua.com.fielden.platform.sample.domain.definers.TgEntityWithPropertyDependencyProp1Definer;
import ua.com.fielden.platform.sample.domain.definers.TgEntityWithPropertyDependencyPropXAndPropYDefiner;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgEntityWithPropertyDependency.class)
@MapEntityTo
public class TgEntityWithPropertyDependency extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Property", desc = "Property")
    @AfterChange(PropDefiner.class)
    @Required
    private String property;
    
    @IsProperty
    @MapTo
    @Title(value = "Dependent Prop", desc = "Dependent Prop")
    private String dependentProp;
    
    @IsProperty
    @Title("Crit-only single property")
    @CritOnly(CritOnly.Type.SINGLE)
    @Required
    private Date critOnlySingleProp;
    
    @IsProperty(value = UserAndRoleAssociation.class, linkProperty = "user")
    private Set<UserAndRoleAssociation> roles = new HashSet<UserAndRoleAssociation>();
    
    //       propX -->___\
    //                ___ > prop1 --> prop2 (dependency through definers: v0 -> val0 -> value0, v1 -> val1 -> value1)
    //       propY -->   /
    
    // touched modified
    // 1) propX := v1
    // 2) prop2 := XXX
    // 3) prop1 := val0
    // 4) prop1 := val1 (=> prop2 == value1)
    
    // untouched modified
    // 1) propX := v0
    // 2) prop2 := XXX
    // 3) propY := v1
    // 4) propY := v0 (=> prop2 == value1, still does not work as expected)
    
    // untouched unmodified
    // 1) propX := v1
    // 2) prop2 := XXX
    // 3) propY := v0
    // 4) propY := v1 (=> prop2 == value0, does not work as expected)
    
    @IsProperty
    @MapTo
    @AfterChange(TgEntityWithPropertyDependencyPropXAndPropYDefiner.class)
    @Title("PropX")
    private String propX;
    
    @IsProperty
    @MapTo
    @AfterChange(TgEntityWithPropertyDependencyPropXAndPropYDefiner.class)
    @Title("PropY")
    private String propY;
    
    @IsProperty
    @MapTo
    @AfterChange(TgEntityWithPropertyDependencyProp1Definer.class)
    @Title("Prop1")
    private String prop1; // initial value 'val0'
    
    @IsProperty
    @MapTo
    @Title("Prop2")
    private String prop2;
    
    @Observable
    public TgEntityWithPropertyDependency setProp2(final String prop2) {
        this.prop2 = prop2;
        return this;
    }
    
    public String getProp2() {
        return prop2;
    }
    
    @Observable
    public TgEntityWithPropertyDependency setProp1(final String prop1) {
        this.prop1 = prop1;
        return this;
    }
    
    public String getProp1() {
        return prop1;
    }
    
    @Observable
    public TgEntityWithPropertyDependency setPropY(final String propY) {
        this.propY = propY;
        return this;
    }
    
    public String getPropY() {
        return propY;
    }
    
    @Observable
    public TgEntityWithPropertyDependency setPropX(final String propX) {
        this.propX = propX;
        return this;
    }
    
    public String getPropX() {
        return propX;
    }
    
    @Observable
    public TgEntityWithPropertyDependency setCritOnlySingleProp(final Date dateTo) {
        this.critOnlySingleProp = dateTo;
        return this;
    }

    public Date getCritOnlySingleProp() {
        return critOnlySingleProp;
    }
    
    public Set<UserAndRoleAssociation> getRoles() {
        return roles;
    }

    @Observable
    public TgEntityWithPropertyDependency setRoles(final Set<UserAndRoleAssociation> roles) {
        this.roles = roles;
        return this;
    }

    @Observable
    public TgEntityWithPropertyDependency setDependentProp(final String dependentProp) {
        this.dependentProp = dependentProp;
        return this;
    }

    public String getDependentProp() {
        return dependentProp;
    }
    
    @Observable
    public TgEntityWithPropertyDependency setProperty(final String property) {
        this.property = property;
        return this;
    }

    public String getProperty() {
        return property;
    }
}