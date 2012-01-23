package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
public class TgOrgUnit3 extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Parent", desc = "Parent")
    private TgOrgUnit2 parent;

    @Observable
    public TgOrgUnit3 setParent(final TgOrgUnit2 parent) {
	this.parent = parent;
	return this;
    }

    public TgOrgUnit2 getParent() {
	return parent;
    }
    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgOrgUnit3() {
    }
}
