package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@KeyType(String.class)
@MapEntityTo
public class TgtDivision extends AbstractEntity<String> {
}
