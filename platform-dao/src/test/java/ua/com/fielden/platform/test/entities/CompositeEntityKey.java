package ua.com.fielden.platform.test.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@KeyType(String.class)
@KeyTitle(value = "key title", desc = "key description")
@DescTitle(value = "desc title", desc = "desc description")
@MapEntityTo("COMPOSITE_ENTITY_KEY")
@CompanionObject(ICompositeEntityKey.class)
public class CompositeEntityKey extends AbstractEntity<String> {

    private static final long serialVersionUID = -7782886590979476991L;

}
