package ua.com.fielden.platform.serialisation.jackson;

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
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IEntityTypeProp.class)
public class EntityTypeProp extends AbstractEntity<String> {

    @IsProperty
    @Title(value = "Secrete", desc = "Determines whether the property represents 'secrete' property (e.g. passwords etc.).")
    private Boolean _secrete;

    @IsProperty
    @Title(value = "Upper Case", desc = "Determines whether the property represents 'upper-case' property.")
    private Boolean _upperCase;

    @IsProperty
    @Title(value = "Title", desc = "The title of the property.")
    private String _title;

    @IsProperty
    @Title(value = "Description", desc = "The description of the property.")
    private String _desc;

    @IsProperty
    @Title(value = "Crit Only", desc = "Determines whether the property is for criteria only.")
    private Boolean _critOnly;

    @IsProperty
    @Title(value = "Result Only", desc = "Determines whether the property is for result only.")
    private Boolean _resultOnly;

    @IsProperty
    @Title(value = "Ignore", desc = "Determines whether the property should be ignored for UI elements.")
    private Boolean _ignore;

    @IsProperty
    @Title(value = "Length", desc = "Represents the length of the underlying db column and can be used for UI editors construction.")
    private Long _length;

    @IsProperty
    @Title(value = "Precision", desc = "Represents the precision of the underlying db column and can be used for UI editors construction.")
    private Long _precision;

    @IsProperty
    @Title(value = "Scale", desc = "Represents the scale of the underlying db column and can be used for UI editors construction.")
    private Long _scale;

    @IsProperty
    @Title(value = "TimeZone", desc = "TimeZone of date-typed property.")
    private String _timeZone;

    @IsProperty
    @Title(value = "Is Date Only?", desc = "Should display only date portion?")
    private Boolean _date;

    @IsProperty
    @Title(value = "Is Time Only?", desc = "Should display only time portion?")
    private Boolean _time;

    @IsProperty
    @Title(value = "Show Trailing Zeros?", desc = "Should display trailing zeros?")
    private boolean _trailingZeros;

    @IsProperty
    @Title(value = "Display  pattern", desc = "Pattern that is used to display value for entity type property.")
    private String _displayAs;

    @IsProperty
    @Title(value = "Type Name", desc = "Name of property type. Full class names for entity-typed properties (prepended with ':'), simple class names for others. Empty if not supported.")
    private String _typeName;

    @IsProperty
    @Title(value = "Short Collection Key", desc = "Non-parent composite key member of this short collectional property's element type (non-empty only if it is short collection).")
    private String _shortCollectionKey;

    @Observable
    public EntityTypeProp set_shortCollectionKey(final String _shortCollectionKey) {
        this._shortCollectionKey = _shortCollectionKey;
        return this;
    }

    public String get_shortCollectionKey() {
        return _shortCollectionKey;
    }

    @Observable
    public EntityTypeProp set_typeName(final String _typeName) {
        this._typeName = _typeName;
        return this;
    }

    public String get_typeName() {
        return _typeName;
    }

    @Observable
    public EntityTypeProp set_displayAs(final String _displayAs) {
        this._displayAs = _displayAs;
        return this;
    }

    public String get_displayAs() {
        return _displayAs;
    }

    @Observable
    public EntityTypeProp set_trailingZeros(final boolean _trailingZeros) {
        this._trailingZeros = _trailingZeros;
        return this;
    }

    public boolean get_trailingZeros() {
        return _trailingZeros;
    }

    @Observable
    public EntityTypeProp set_time(final Boolean _time) {
        this._time = _time;
        return this;
    }

    public Boolean get_time() {
        return _time;
    }

    @Observable
    public EntityTypeProp set_date(final Boolean _date) {
        this._date = _date;
        return this;
    }

    public Boolean get_date() {
        return _date;
    }

    @Observable
    public EntityTypeProp set_timeZone(final String _timeZone) {
        this._timeZone = _timeZone;
        return this;
    }

    public String get_timeZone() {
        return _timeZone;
    }

    @Observable
    public EntityTypeProp set_scale(final Long _scale) {
        this._scale = _scale;
        return this;
    }

    public Long get_scale() {
        return _scale;
    }

    @Observable
    public EntityTypeProp set_precision(final Long _precision) {
        this._precision = _precision;
        return this;
    }

    public Long get_precision() {
        return _precision;
    }

    @Observable
    public EntityTypeProp set_length(final Long _length) {
        this._length = _length;
        return this;
    }

    public Long get_length() {
        return _length;
    }

    @Observable
    public EntityTypeProp set_ignore(final Boolean _ignore) {
        this._ignore = _ignore;
        return this;
    }

    public Boolean get_ignore() {
        return _ignore;
    }

    @Observable
    public EntityTypeProp set_resultOnly(final Boolean _resultOnly) {
        this._resultOnly = _resultOnly;
        return this;
    }

    public Boolean get_resultOnly() {
        return _resultOnly;
    }

    @Observable
    public EntityTypeProp set_critOnly(final Boolean _critOnly) {
        this._critOnly = _critOnly;
        return this;
    }

    public Boolean get_critOnly() {
        return _critOnly;
    }

    @Observable
    public EntityTypeProp set_desc(final String _desc) {
        this._desc = _desc;
        return this;
    }

    public String get_desc() {
        return _desc;
    }

    @Observable
    public EntityTypeProp set_title(final String _title) {
        this._title = _title;
        return this;
    }

    public String get_title() {
        return _title;
    }

    @Observable
    public EntityTypeProp set_upperCase(final Boolean _upperCase) {
        this._upperCase = _upperCase;
        return this;
    }

    public Boolean get_upperCase() {
        return _upperCase;
    }

    @Observable
    public EntityTypeProp set_secrete(final Boolean _secrete) {
        this._secrete = _secrete;
        return this;
    }

    public Boolean get_secrete() {
        return _secrete;
    }

}