package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(String.class)
@KeyTitle(value = "simple entity")
@DescTitle(value = "Simple description")
public class SimpleEntityWithCommonProperties extends AbstractEntity<String> {

    @IsProperty
    private String commonProperty;

    @IsProperty
    private SimpleEntity uncommonProperty;

}