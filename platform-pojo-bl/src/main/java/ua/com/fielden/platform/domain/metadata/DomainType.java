package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@KeyType(String.class)
@KeyTitle("Type full name")
@DescTitle("Description")
@DescRequired
@CompanionObject(IDomainType.class)
@MapEntityTo
public class DomainType extends AbstractEntity<String> {

}
