package ua.com.fielden.platform.entity.union;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public class UnionEntityWithKindOneError extends AbstractUnionEntity {
    @IsProperty
    @Title(value = "Correct property", desc = "Desc")
    private EntityOne entity;

    @IsProperty
    @Title(value = "Incorrect property", desc = "Desc")
    private String string;

    public EntityOne getEntity() {
        return entity;
    }

    @Observable
    public void setEntity(final EntityOne entity) {
        this.entity = entity;
    }

    public String getString() {
        return string;
    }

    @Observable
    public void setString(final String string) {
        this.string = string;
    }

}
