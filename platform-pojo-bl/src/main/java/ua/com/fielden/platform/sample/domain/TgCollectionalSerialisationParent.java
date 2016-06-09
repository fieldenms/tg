package ua.com.fielden.platform.sample.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
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
@DescTitle("Desc")
@CompanionObject(ITgCollectionalSerialisationParent.class)
@MapEntityTo
public class TgCollectionalSerialisationParent extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(TgCollectionalSerialisationChild.class)
    @Title(value = "Collectional prop", desc = "Collectional prop")
    private Set<TgCollectionalSerialisationChild> collProp = new HashSet<TgCollectionalSerialisationChild>();

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