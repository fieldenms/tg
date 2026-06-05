package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@KeyTitle(value = "key", desc = "key description")
@DescTitle(value = "description", desc = "desc description")
public class SimplePartEntity extends AbstractEntity<String> {

    @IsProperty
    @Title(value = "Common property", desc = "Common property description")
    private String commonProperty;

    @IsProperty
    @Title(value = "Level property", desc = "Level property description")
    private SecondLevelEntity levelEntity;

    @IsProperty
    @Title(value = "Uncommon property", desc = "Uncommon proerty description")
    private String uncommonProperty;

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

    @Override
    @Observable
    public SimplePartEntity setDesc(String desc) {
        return super.setDesc(desc);
    }

}
