package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@DescTitle("Desc")
@CompanionObject(ITgCollectionalSerialisationParent.class)
@MapEntityTo
public class TgCollectionalSerialisationParent extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(TgCollectionalSerialisationChild.class)
    @Title(value = "Collectional prop", desc = "Collectional prop")
    private Set<TgCollectionalSerialisationChild> collProp = new HashSet<TgCollectionalSerialisationChild>();

    @IsProperty(length = 255)
    @MapTo
    @Title("Desc")
    @BeforeChange(@Handler(TgCollectionalSerialisationParentDescValidator.class))
    private String desc;

    @Observable
    public TgCollectionalSerialisationParent setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    @Observable
    protected TgCollectionalSerialisationParent setCollProp(final Set<TgCollectionalSerialisationChild> collProp) {
        this.collProp.clear();
        this.collProp.addAll(collProp);
        return this;
    }

    public Set<TgCollectionalSerialisationChild> getCollProp() {
        return Collections.unmodifiableSet(collProp);
    }

}