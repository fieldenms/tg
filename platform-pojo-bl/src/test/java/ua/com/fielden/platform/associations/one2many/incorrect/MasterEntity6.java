package ua.com.fielden.platform.associations.one2many.incorrect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * The master type in One-to-Many association with a collectional and single (special case) properties representing assocaitons.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class MasterEntity6 extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    private DetailsEntity6 illegalOne2oneAssociation;

    public DetailsEntity6 getIllegalOne2oneAssociation() {
        return illegalOne2oneAssociation;
    }
    @Observable
    public void setIllegalOne2oneAssociation(final DetailsEntity6 illegalOne2oneAssociation) {
        this.illegalOne2oneAssociation = illegalOne2oneAssociation;
    }
}
