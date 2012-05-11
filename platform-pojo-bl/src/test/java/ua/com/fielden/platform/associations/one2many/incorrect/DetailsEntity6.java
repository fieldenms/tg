package ua.com.fielden.platform.associations.one2many.incorrect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Type representing the details side of One-to-One association.
 *
 * @author TG Team
 *
 */
@KeyType(MasterEntity1.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class DetailsEntity6 extends AbstractEntity<MasterEntity1 /* Non-parent key type */> {
    private static final long serialVersionUID = 1L;
}
