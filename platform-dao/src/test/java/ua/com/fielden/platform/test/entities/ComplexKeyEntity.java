package ua.com.fielden.platform.test.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@KeyType(CompositeEntityKey.class)
@KeyTitle(value = "key title", desc = "key description")
@DescTitle(value = "desc title", desc = "desc description")
@MapEntityTo("COMPLEX_KEY_ENTITY")
@CompanionObject(IComplexKeyEntity.class)
public class ComplexKeyEntity extends AbstractEntity<CompositeEntityKey> {

    private static final long serialVersionUID = -8837409604335848090L;

}
