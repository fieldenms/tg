package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Dynamic key")
@DescTitle(value = "Dynamic description")
public class DynamicKeyEntity extends AbstractEntity<DynamicEntityKey> {

    private static final long serialVersionUID = 1779449465249616599L;

    @IsProperty
    @CompositeKeyMember(1)
    private String firstKeyMemeber;

    @IsProperty
    @CompositeKeyMember(2)
    private String secondKeyMember;

    @IsProperty
    private String commonProperty;

    @IsProperty
    private SimpleEntity uncommonProperty;
}
