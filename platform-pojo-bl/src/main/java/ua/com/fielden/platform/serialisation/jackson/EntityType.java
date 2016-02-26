package ua.com.fielden.platform.serialisation.jackson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
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
@CompanionObject(IEntityType.class)
public class EntityType extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Number", desc = "Number of the type in context of other types for serialisation")
    private Long _number;

    @IsProperty(String.class)
    @Title(value = "Composite Keys", desc = "Composite key property names")
    private List<String> _compositeKeyNames;

    @IsProperty
    @Title(value = "Composite Key Separator", desc = "Separator for composite key members (for autocompletion)")
    private String _compositeKeySeparator;

    @IsProperty
    @Title(value = "Entity Title", desc = "Entity title")
    private String _entityTitle;

    @IsProperty
    @Title(value = "Entity Desc", desc = "Entity description")
    private String _entityDesc;

    @IsProperty(EntityTypeProp.class)
    @Title(value = "Entity Type Properties", desc = "A map of entity type properties by their names")
    private Map<String, EntityTypeProp> _props;

    @IsProperty
    @Title(value = "Is Persistent?", desc = "Indicated whether the associated entity type represents a persistent entity.")
    private boolean _persistent;

    @Observable
    public EntityType set_persistent(final boolean _persistent) {
        this._persistent = _persistent;
        return this;
    }

    public boolean is_persistent() {
        return _persistent;
    }

    @Observable
    protected EntityType set_props(final Map<String, EntityTypeProp> _props) {
        this._props = new LinkedHashMap<>();

        this._props.clear();
        this._props.putAll(_props);
        return this;
    }

    public Map<String, EntityTypeProp> get_props() {
        return Collections.unmodifiableMap(_props);
    }

    @Observable
    public EntityType set_entityDesc(final String entityDesc) {
        this._entityDesc = entityDesc;
        return this;
    }

    public String get_entityDesc() {
        return _entityDesc;
    }

    @Observable
    public EntityType set_entityTitle(final String entityTitle) {
        this._entityTitle = entityTitle;
        return this;
    }

    public String get_entityTitle() {
        return _entityTitle;
    }

    @Observable
    public EntityType set_compositeKeySeparator(final String _compositeKeySeparator) {
        this._compositeKeySeparator = _compositeKeySeparator;
        return this;
    }

    public String get_compositeKeySeparator() {
        return _compositeKeySeparator;
    }

    @Observable
    protected EntityType set_compositeKeyNames(final List<String> _compositeKeyNames) {
        this._compositeKeyNames = new ArrayList<>();

        this._compositeKeyNames.clear();
        this._compositeKeyNames.addAll(_compositeKeyNames);
        return this;
    }

    public List<String> get_compositeKeyNames() {
        return Collections.unmodifiableList(_compositeKeyNames);
    }

    @Observable
    public EntityType set_number(final Long _number) {
        this._number = _number;
        return this;
    }

    public Long get_number() {
        return _number;
    }

}