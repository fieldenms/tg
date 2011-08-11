package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(String.class)
@KeyTitle(value = "key")
@DescTitle(value = "Description")
public class SyntheticEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -7605532319969851761L;

}
