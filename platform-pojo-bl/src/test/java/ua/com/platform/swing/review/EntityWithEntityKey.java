package ua.com.platform.swing.review;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(KeyEntity.class)
@KeyTitle(value = "entity key title", desc = "entity key title description")
@DescTitle(value = "entity desc", desc = "entity desc description")
public class EntityWithEntityKey extends AbstractEntity<KeyEntity> {

    /**
     * 
     */
    private static final long serialVersionUID = 5810633884185876504L;

    @IsProperty
    @Title(value = "key entity property", desc = "key entity property description")
    private KeyEntity keyEntity;

    public KeyEntity getKeyEntity() {
        return keyEntity;
    }

    @Observable
    public void setKeyEntity(final KeyEntity keyEntity) {
        this.keyEntity = keyEntity;
    }

}
