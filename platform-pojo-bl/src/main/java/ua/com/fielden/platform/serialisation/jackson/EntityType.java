package ua.com.fielden.platform.serialisation.jackson;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.master.MasterInfo;

import java.util.*;

/**
 * The entity type to represent serialisable entity types for client-side handling.
 * `tg-reflector` defines how this data are interpreted.
 * <p>
 * <code>null</code> values are not serialised. This can be used to reduce resultant JSON.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity Type Name", desc = "Entity Type Name description")
@CompanionObject(IEntityType.class)
public class EntityType extends AbstractEntity<String> {
    @IsProperty(String.class)
    @Title(value = "Composite Keys", desc = "Composite key property names")
    private final List<String> _compositeKeyNames = new ArrayList<>();

    @IsProperty
    @Title(value = "Composite Key Separator", desc = "Separator for composite key members (for autocompletion)")
    private String _compositeKeySeparator;

    @IsProperty
    @Title(value = "Key Title", desc = "The title of the composite / union key.")
    private String _keyTitle;

    @IsProperty
    @Title(value = "Key Description", desc = "The description of the composite / union key.")
    private String _keyDesc;

    @IsProperty
    @Title(value = "Entity Title", desc = "Entity title")
    private String _entityTitle;

    @IsProperty
    @Title(value = "Entity Desc", desc = "Entity description")
    private String _entityDesc;

    @IsProperty(EntityTypeProp.class)
    @Title(value = "Entity Type Properties", desc = "A map of entity type properties by their names")
    private final Map<String, EntityTypeProp> _props = new LinkedHashMap<>();

    @IsProperty
    @Title(value = "Is Persistent?", desc = "Indicates whether the associated entity type represents a persistent entity.")
    private Boolean _persistent;

    @IsProperty
    @Title(value = "Persistent with version?", desc = "Indicates whether the associated entity type represents a persistent entity with version data.")
    private Boolean _persistentWithVersion;

    @IsProperty
    @Title(value = "Is Audited?", desc = "Indicates whether the associated entity type is audited.")
    private Boolean _audited;

    @IsProperty
    @Title(value = "Should Display Description?", desc = "Indicates whether editors for values of this type should display values descriptions")
    private Boolean _displayDesc;

    @IsProperty
    @Title(value = "Is Continuation?", desc = "Indicates whether the associated entity type represents a continuation entity.")
    private Boolean _continuation;

    @IsProperty
    @Title(value = "Union Common Properties", desc = "The list of common properties (can be empty) in case if the associated entity type represents union entity type; null otherwise.")
    private List<String> _unionCommonProps; // intentionally null (i.e. not serialised) to differentiate between [empty set of common properties for union entity type] and [non-union entity type]

    @IsProperty
    @Title(value = "Union Properties", desc = "The list of union properties in case if asscoiated entity type represents union entity type; null otherwise")
    private List<String> _unionProps;

    @IsProperty
    @Title(value = "Compound Opener Type", desc = "Represents main persistent type for this compound master opener (if it is of such kind, empty otherwise).")
    private String _compoundOpenerType;

    @IsProperty
    @Title(value = "Is Compound Menu Item?", desc = "Indicates whether the associated entity type represents menu item entity in compound master.")
    private Boolean _compoundMenuItem;

    @IsProperty
    @Title(value = "Entity Master", desc = "Entity Master Data")
    private MasterInfo _entityMaster;

    @IsProperty
    @Title(value = "New Entity Master", desc = "Entity master data for new entity action")
    private MasterInfo _newEntityMaster;

    @IsProperty
    @Title(value = "Base Type", desc = "Represents base type for this synthetic-based-on-persistent / single-entity-key type (if it is of such kind, empty otherwise).")
    private String _baseType;

    @Observable
    public EntityType set_baseType(final String value) {
        this._baseType = value;
        return this;
    }

    public String get_baseType() {
        return _baseType;
    }

    @Observable
    public EntityType set_newEntityMaster(final MasterInfo _newEntityMaster) {
        this._newEntityMaster = _newEntityMaster;
        return this;
    }

    public MasterInfo get_newEntityMaster() {
        return _newEntityMaster;
    }

    @Observable
    public EntityType set_entityMaster(final MasterInfo _entityMaster) {
        this._entityMaster = _entityMaster;
        return this;
    }

    public MasterInfo get_entityMaster() {
        return _entityMaster;
    }

    @Observable
    public EntityType set_compoundMenuItem(final Boolean _compoundMenuItem) {
        this._compoundMenuItem = _compoundMenuItem;
        return this;
    }

    public Boolean is_compoundMenuItem() {
        return _compoundMenuItem;
    }

    @Observable
    public EntityType set_compoundOpenerType(final String value) {
        this._compoundOpenerType = value;
        return this;
    }

    public String get_compoundOpenerType() {
        return _compoundOpenerType;
    }

    @Observable
    public EntityType set_unionCommonProps(final List<String> value) {
        this._unionCommonProps = value;
        return this;
    }

    public List<String> get_unionCommonProps() {
        return _unionCommonProps;
    }

    @Observable
    public EntityType set_unionProps(final List<String> _unionProps) {
        this._unionProps = _unionProps;
        return this;
    }

    public List<String> get_unionProps() {
        return _unionProps;
    }

    @Observable
    public EntityType set_displayDesc(final Boolean _displayDesc) {
        this._displayDesc = _displayDesc;
        return this;
    }

    public Boolean get_displayDesc() {
        return _displayDesc;
    }

    public Boolean get_persistentWithVersion() {
        return _persistentWithVersion;
    }

    @Observable
    public EntityType set_persistentWithVersion(final Boolean _persistentWithVersion) {
        this._persistentWithVersion = _persistentWithVersion;
        return this;
    }

    public Boolean get_audited() {
        return _audited;
    }

    @Observable
    public EntityType set_audited(final Boolean _audited) {
        this._audited = _audited;
        return this;
    }

    @Observable
    public EntityType set_persistent(final Boolean _persistent) {
        this._persistent = _persistent;
        return this;
    }

    public Boolean is_persistent() {
        return _persistent;
    }

    @Observable
    public EntityType set_continuation(final Boolean _continuation) {
        this._continuation = _continuation;
        return this;
    }

    public Boolean is_continuation() {
        return _continuation;
    }

    @Observable
    protected EntityType set_props(final Map<String, EntityTypeProp> _props) {
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
    public EntityType set_keyDesc(final String _keyDesc) {
        this._keyDesc = _keyDesc;
        return this;
    }

    public String get_keyDesc() {
        return _keyDesc;
    }

    @Observable
    public EntityType set_keyTitle(final String _keyTitle) {
        this._keyTitle = _keyTitle;
        return this;
    }

    public String get_keyTitle() {
        return _keyTitle;
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
        this._compositeKeyNames.clear();
        this._compositeKeyNames.addAll(_compositeKeyNames);
        return this;
    }

    public List<String> get_compositeKeyNames() {
        return Collections.unmodifiableList(_compositeKeyNames);
    }
}
