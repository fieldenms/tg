package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;

// @CompanionObject(IDriverImportRowError.class)
@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgDefinersExecutorCollectionalChild extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty
    @MapTo
    @Title(value = "First Member", desc = "First Menber")
    @CompositeKeyMember(1)
    private TgDefinersExecutorParent member1;
    
    @IsProperty
    @MapTo
    @Title(value = "Second member", desc = "Desc")
    @CompositeKeyMember(2)
    @AfterChange(TgDefinersExecutorCollectionalChildHandler.class)
    private String member2;

    @Observable
    public TgDefinersExecutorCollectionalChild setMember2(final String member2) {
        this.member2 = member2;
        return this;
    }

    public String getMember2() {
        return member2;
    }

    @Observable
    public TgDefinersExecutorCollectionalChild setMember1(final TgDefinersExecutorParent member1) {
        this.member1 = member1;
        return this;
    }

    public TgDefinersExecutorParent getMember1() {
        return member1;
    }
}
