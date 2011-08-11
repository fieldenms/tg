package ua.com.platform.swing.review;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "composite key title", desc = "composite key title description")
@DescTitle(value = "composite key entity desc", desc = "composite key entity desc description")
public class EntityWithCompositeKey extends AbstractEntity<DynamicEntityKey> {

    /**
     * 
     */
    private static final long serialVersionUID = -9289783279448660L;

    @IsProperty
    @Title(value = "first key member", desc = "first key member description")
    @CompositeKeyMember(1)
    private String firstKeyMember;

    @IsProperty
    @Title(value = "second key member", desc = "second key member description")
    @CompositeKeyMember(2)
    private String secondKeyMember;

    @IsProperty
    @Title(value = "key entity member", desc = "key entity member description")
    @CompositeKeyMember(3)
    private KeyEntity keyEntityMember;

    protected EntityWithCompositeKey() {
	setKey(new DynamicEntityKey(this));
    }

    public String getFirstKeyMember() {
	return firstKeyMember;
    }

    public String getSecondKeyMember() {
	return secondKeyMember;
    }

    public KeyEntity getKeyEntityMember() {
	return keyEntityMember;
    }

    @Observable
    public void setFirstKeyMember(final String firstKeyMember) {
	this.firstKeyMember = firstKeyMember;
    }

    @Observable
    public void setSecondKeyMember(final String secondKeyMember) {
	this.secondKeyMember = secondKeyMember;
    }

    @Observable
    public void setKeyEntityMember(final KeyEntity keyEntityMember) {
	this.keyEntityMember = keyEntityMember;
    }
}
