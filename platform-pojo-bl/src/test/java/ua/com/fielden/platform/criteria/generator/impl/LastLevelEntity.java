package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Entity for testing purposes.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("key")
@DescTitle("desc")
public class LastLevelEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -7989043784346226378L;

}
