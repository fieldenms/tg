package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.SINGLE;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;

/**
 * Parent entity for Definers Executor Test.
 *
 * @author TG Team
 *
 */
// @CompanionObject(IDriverImportRow.class)
@MapEntityTo
@KeyType(DynamicEntityKey.class)
public class TgDefinersExecutorParent extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = -5945674491809287858L;
    
    @IsProperty(TgDefinersExecutorCollectionalChild.class)
    @AfterChange(TgDefinersExecutorParentHandlerForCollection.class)
    private final Set<TgDefinersExecutorCollectionalChild> collectionWithHandler = new LinkedHashSet<TgDefinersExecutorCollectionalChild>();
    
    @IsProperty
    @MapTo
    @Title(value = "Prop with handler", desc = "Desc")
    @AfterChange(TgDefinersExecutorParentHandler.class)
    private String propWithHandler;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String keyMember2;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgDefinersExecutorCompositeKeyMember keyMember1;
    
    @IsProperty
    @CritOnly(SINGLE)
    @Title("Crit-Only Single Prop with handler")
    @AfterChange(TgDefinersExecutorParentHandler.class)
    private String critOnlySinglePropWithHandler;
    
    @Observable
    public TgDefinersExecutorParent setCritOnlySinglePropWithHandler(final String critOnlySinglePropWithHandler) {
        this.critOnlySinglePropWithHandler = critOnlySinglePropWithHandler;
        return this;
    }
    
    public String getCritOnlySinglePropWithHandler() {
        return critOnlySinglePropWithHandler;
    }
    
    @Observable
    public TgDefinersExecutorParent setPropWithHandler(final String propWithHandler) {
        this.propWithHandler = propWithHandler;
        return this;
    }

    public String getPropWithHandler() {
        return propWithHandler;
    }

    @Observable
    public TgDefinersExecutorParent setCollectionWithHandler(final Set<TgDefinersExecutorCollectionalChild> collectionWithHandler) {
        this.collectionWithHandler.clear();
        this.collectionWithHandler.addAll(collectionWithHandler);
        return this;
    }

    public Set<TgDefinersExecutorCollectionalChild> getCollectionWithHandler() {
        return Collections.unmodifiableSet(collectionWithHandler);
    }

    @Observable
    public void setKeyMember1(final TgDefinersExecutorCompositeKeyMember keyMember1) {
        this.keyMember1 = keyMember1;
    }

    public TgDefinersExecutorCompositeKeyMember getKeyMember1() {
        return keyMember1;
    }
    
    @Observable
    public void setKeyMember2(final String keyMember2) {
        this.keyMember2 = keyMember2;
    }

    public String getKeyMember2() {
        return keyMember2;
    }
}
