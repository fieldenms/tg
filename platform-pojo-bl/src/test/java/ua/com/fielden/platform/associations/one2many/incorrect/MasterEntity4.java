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
public class MasterEntity4 extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty // linkProperty="key1" is missing on purpose
    @MapTo
    private DetailsEntity4 one2manyAssociationSpecialCase;

    public DetailsEntity4 getOne2manyAssociationSpecialCase() {
        return one2manyAssociationSpecialCase;
    }
    @Observable
    public void setOne2manyAssociationSpecialCase(final DetailsEntity4 one2manyAssociationSpecialCase) {
        this.one2manyAssociationSpecialCase = one2manyAssociationSpecialCase;
    }
}
