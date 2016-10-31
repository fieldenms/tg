package ua.com.fielden.platform.data.generator.skipe_demo;

import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;

public class EntityToGen extends AbstractEntity<String> implements WithCreatedByUser<EntityToGen> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private User createdBy;

    @Observable
    public EntityToGen setCreatedBy(final User generatedBy) {
        this.createdBy = generatedBy;
        return this;
    }

    public User getCreatedBy() {
        return createdBy;
    }


}
