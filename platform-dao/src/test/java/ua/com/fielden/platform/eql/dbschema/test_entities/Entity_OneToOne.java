package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/// A persistent one-2-one test entity — its key is a reference to another persistent entity.
/// Used to verify that a foreign key is generated for the `_ID` column.
///
@MapEntityTo
@KeyType(Entity_Simple.class)
public class Entity_OneToOne extends AbstractEntity<Entity_Simple> {
}
