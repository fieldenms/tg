package ua.com.fielden.platform.entity;

import java.util.Arrays;

import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity class used for testing byte array property behaviour.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired
public class EntityWithByteArray extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "First Property", desc = "used for testing")
    private byte[] byteArray = null;

    public byte[] getByteArray() {
        return Arrays.copyOf(byteArray, byteArray.length);
    }

    @Observable
    public void setByteArray(final byte[] byteArray) {
        this.byteArray = byteArray;
    }



}