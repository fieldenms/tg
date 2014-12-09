package ua.com.fielden.platform.serialisation.jackson;

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
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IEntityTypeProp.class)
public class EntityTypeProp extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Secrete", desc = "Determines whether the property represents 'secrete' property (e.g. passwords etc.)")
    private Boolean _secrete;

    @IsProperty
    @MapTo
    @Title(value = "Upper Case", desc = "Determines whether the property represents 'upper-case' property")
    private Boolean _upperCase;

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