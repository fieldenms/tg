package ua.com.fielden.platform.associations.one2many.incorrect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

import static java.util.Collections.unmodifiableList;

/**
 * The master type in One-to-Many association with a collectional and single (special case) properties representing assocaitons.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class MasterEntity2 extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(linkProperty = "key1")
    @MapTo
    private final List<DetailsEntity2> one2manyAssociationCollectional = new ArrayList<>();

    @Observable
    public MasterEntity2 setOne2manyAssociationCollectional(final List<DetailsEntity2> one2manyAssociationCollectional) {
        this.one2manyAssociationCollectional.clear();
        this.one2manyAssociationCollectional.addAll(one2manyAssociationCollectional);
        return this;
    }

    public List<DetailsEntity2> getOne2manyAssociationCollectional() {
        return unmodifiableList(one2manyAssociationCollectional);
    }
}
