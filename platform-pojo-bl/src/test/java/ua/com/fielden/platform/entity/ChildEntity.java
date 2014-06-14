package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.error.Result;

/**
 * Entity class used for testing.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired
public class ChildEntity extends AbstractEntity<String> {

    protected ChildEntity() {
	setDesc("lala");
    }

    @Override
    @Observable
    public ChildEntity setDesc(final String desc) {
	super.setDesc(desc);
	return this;
    }

    @Override
    protected Result validate() {
	return super.validate();
    }

}