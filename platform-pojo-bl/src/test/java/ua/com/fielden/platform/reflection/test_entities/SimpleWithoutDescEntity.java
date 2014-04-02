package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "key", desc = "key description")
public class SimpleWithoutDescEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -7795529065956315280L;

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

}
