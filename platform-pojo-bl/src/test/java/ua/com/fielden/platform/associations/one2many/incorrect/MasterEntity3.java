package ua.com.fielden.platform.associations.one2many.incorrect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
@MapEntityTo
public class MasterEntity3 extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(value = DetailsEntity3.class)
    @MapTo
    private final List<DetailsEntity3> one2manyAssociationCollectional = new ArrayList<>();

    @Observable
    public MasterEntity3 setOne2manyAssociationCollectional(final List<DetailsEntity3> one2manyAssociationCollectional) {
        this.one2manyAssociationCollectional.clear();
        this.one2manyAssociationCollectional.addAll(one2manyAssociationCollectional);
        return this;
    }

    public List<DetailsEntity3> getOne2manyAssociationCollectional() {
        return unmodifiableList(one2manyAssociationCollectional);
    }

}
