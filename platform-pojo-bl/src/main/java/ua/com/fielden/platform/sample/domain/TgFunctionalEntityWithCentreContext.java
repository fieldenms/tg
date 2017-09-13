package ua.com.fielden.platform.sample.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An example functional entity to be assigned to centre actions.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgFunctionalEntityWithCentreContext.class)
@DescTitle(value = "Desc", desc = "Some desc description")
@EntityTitle(value = "Func ent title", desc = "Func entity description")
public class TgFunctionalEntityWithCentreContext extends AbstractFunctionalEntityWithCentreContext<String> {
    @IsProperty
    @Title(value = "Value To Insert", desc = "The string value to be inserted for stringProp of all selected entities")
    private String valueToInsert; // this property is intended to be context-dependent

    @IsProperty
    @Title(value = "With Brackets", desc = "Indicates whether the stringProp props should be wrapped by brackets after insertion")
    private boolean withBrackets;
    
    @IsProperty(Long.class)
    @Title("Selected Entity IDs")
    private Set<Long> selectedEntityIds = new HashSet<>();
    
    @IsProperty
    @Title("User Param")
    private String userParam;

    @Observable
    public TgFunctionalEntityWithCentreContext setUserParam(final String userParam) {
        this.userParam = userParam;
        return this;
    }
    
    public String getUserParam() {
        return userParam;
    }
    
    @Observable
    protected TgFunctionalEntityWithCentreContext setSelectedEntityIds(final Set<Long> selectedEntityIds) {
        this.selectedEntityIds.clear();
        this.selectedEntityIds.addAll(selectedEntityIds);
        return this;
    }

    public Set<Long> getSelectedEntityIds() {
        return Collections.unmodifiableSet(selectedEntityIds);
    }

    @Observable
    public TgFunctionalEntityWithCentreContext setWithBrackets(final boolean withBrackets) {
        this.withBrackets = withBrackets;
        return this;
    }

    public boolean getWithBrackets() {
        return withBrackets;
    }

    @Observable
    public TgFunctionalEntityWithCentreContext setValueToInsert(final String valueToInsert) {
        this.valueToInsert = valueToInsert;
        return this;
    }

    public String getValueToInsert() {
        return valueToInsert;
    }
}