package ua.com.fielden.platform.test.entities;

import ua.com.fielden.platform.entity.SyntheticEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
@KeyTitle(value = "synth key", desc = "synth key description")
@DescTitle(value = "synth desc", desc = "synth desc description")
public class SimpleSyntheticEntity extends SyntheticEntity {

    private static final long serialVersionUID = -8723167457260881172L;

    @IsProperty
    private CompositeEntity firstComponent;

    @IsProperty
    private CompositeEntityKey secondComponent;

    public CompositeEntity getFirstComponent() {
	return firstComponent;
    }

    public CompositeEntityKey getSecondComponent() {
	return secondComponent;
    }

    @Observable
    public void setFirstComponent(final CompositeEntity firstComponent) {
	this.firstComponent = firstComponent;
    }

    @Observable
    public void setSecondComponent(final CompositeEntityKey secondComponent) {
	this.secondComponent = secondComponent;
    }
}
