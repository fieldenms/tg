package ua.com.fielden.platform.serialisation.jackson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity Type Name", desc = "Entity Type Name description")
@CompanionObject(IEntityTypeInfo.class)
public class EntityTypeInfo extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Number", desc = "Number of the type in context of other types for serialisation")
    private Long number;

    @IsProperty(String.class)
    @Title(value = "Composite Keys", desc = "Composite key property names")
    private List<String> compositeKeyNames;

    @IsProperty
    @Title(value = "Composite Key Member Separator", desc = "Composite Key Member Separator")
    private String compositeKeySeparator;

    @Observable
    public EntityTypeInfo setCompositeKeySeparator(final String compositeKeySeparator) {
        this.compositeKeySeparator = compositeKeySeparator;
        return this;
    }

    public String getCompositeKeySeparator() {
        return compositeKeySeparator;
    }

    @Observable
    protected EntityTypeInfo setCompositeKeyNames(final List<String> compositeKeyNames) {
        this.compositeKeyNames = new ArrayList<>();

        this.compositeKeyNames.clear();
        this.compositeKeyNames.addAll(compositeKeyNames);
        return this;
    }

    public List<String> getCompositeKeyNames() {
        return Collections.unmodifiableList(compositeKeyNames);
    }

    @Observable
    public EntityTypeInfo setNumber(final Long number) {
        this.number = number;
        return this;
    }

    public Long getNumber() {
        return number;
    }

}