package ua.com.fielden.platform.reflection.test_entities;

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
@KeyTitle(value = "key", desc = "key description")
@DescTitle(value = "description", desc = "desc description")
public class DynamicKeyPartEntity extends AbstractEntity<DynamicEntityKey> {

    private static final long serialVersionUID = -7980727819321782977L;

    @IsProperty
    @Title(value = "Name", desc = "Name description")
    @CompositeKeyMember(1)
    private String name;

    @IsProperty
    @Title(value = "Second key", desc = "Second key description")
    @CompositeKeyMember(2)
    private SecondLevelEntity secondKeyMember;

    @IsProperty
    @Title(value = "Common property", desc = "Common property description")
    private String commonProperty;

    @IsProperty
    @Title(value = "Level property", desc = "Level property description")
    private SecondLevelEntity levelEntity;

    @IsProperty
    @Title(value = "Uncommon property", desc = "Uncommon property description")
    private String uncommonProperty;

    protected DynamicKeyPartEntity() {
        setKey(new DynamicEntityKey(this));
    }

    public String getName() {
        return name;
    }

    @Observable
    public void setName(final String name) {
        this.name = name;
    }

    public SecondLevelEntity getSecondKeyMember() {
        return secondKeyMember;
    }

    @Observable
    public void setSecondKeyMember(final SecondLevelEntity secondKeyMember) {
        this.secondKeyMember = secondKeyMember;
    }

    public String getCommonProperty() {
        return commonProperty;
    }

    @Observable
    public void setCommonProperty(final String commonProperty) {
        this.commonProperty = commonProperty;
    }

    public SecondLevelEntity getLevelEntity() {
        return levelEntity;
    }

    @Observable
    public void setLevelEntity(final SecondLevelEntity levelEntity) {
        this.levelEntity = levelEntity;
    }

    public String getUncommonProperty() {
        return uncommonProperty;
    }

    @Observable
    public void setUncommonProperty(final String uncommonProperty) {
        this.uncommonProperty = uncommonProperty;
    }

}
