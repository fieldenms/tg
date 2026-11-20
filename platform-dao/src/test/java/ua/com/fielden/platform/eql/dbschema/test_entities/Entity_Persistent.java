package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.security.user.User;

/// A persistent test entity that inherits the audit-trail properties of [AbstractPersistentEntity],
/// including `createdBy` and `lastUpdatedBy`, which reference [User].
/// Used to verify that those two properties are excluded from index generation, while other entity-typed properties are not.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_Persistent extends AbstractPersistentEntity<String> {

    @IsProperty
    @MapTo
    private User assignee;

    public User getAssignee() {
        return assignee;
    }

    @Observable
    public Entity_Persistent setAssignee(final User assignee) {
        this.assignee = assignee;
        return this;
    }

}
