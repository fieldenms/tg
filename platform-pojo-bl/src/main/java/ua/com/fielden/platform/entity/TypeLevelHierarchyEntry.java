package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle("Entity Type Title")
@DescTitle("Entity Type Description")
public class TypeLevelHierarchyEntry extends AbstractEntity<String> {

    @IsProperty
    @Title(value = "Number of Entities", desc = "The number of entities of this type those refrences the entity of upper level entity")
    private Integer numberOfEntities;

    @Observable
    public TypeLevelHierarchyEntry setNumberOfEntities(final Integer numberOfEntities) {
        this.numberOfEntities = numberOfEntities;
        return this;
    }

    public Integer getNumberOfEntities() {
        return numberOfEntities;
    }
}
