package ua.com.fielden.platform.reflection.test_entities;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Test class, which represent an entity derived directly from AbstractEntity with collection properties.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
public class EntityWithCollection extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(Double.class)
    private List<Double> collection;

    @IsProperty(CollectionalEntity.class)
    private List<CollectionalEntity> collection2;

    protected EntityWithCollection() {
    }

    @Observable
    public void setCollection(final List<Double> collection) {
        this.collection = collection;
    }

    public List<Double> getCollection() {
        return collection;
    }

    @Observable
    public void setCollection2(final List<CollectionalEntity> collection2) {
        this.collection2 = collection2;
    }

    public List<CollectionalEntity> getCollection2() {
        return collection2;
    }
}
