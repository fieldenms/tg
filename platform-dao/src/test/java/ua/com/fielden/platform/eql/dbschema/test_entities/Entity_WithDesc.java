package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/// A persistent test entity that exposes the inherited `desc` property by way of [DescTitle].
/// Used to verify that a column for `desc` is generated only for entities that expose it.
///
@MapEntityTo
@KeyType(String.class)
@DescTitle("Description")
public class Entity_WithDesc extends AbstractEntity<String> {
}
