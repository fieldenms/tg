package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Test class, which represent an entity derived directly from AbstractEntity with collection properties.
 *
 * @author 01es
 */
@KeyType(String.class)
public class EntityWithCollection extends AbstractEntity<String> {

    @IsProperty(Double.class)
    private final List<Double> collection = new ArrayList<>();

    @IsProperty(CollectionalEntity.class)
    private final List<CollectionalEntity> collection2 = new ArrayList<>();

    protected EntityWithCollection() {
    }

    @Observable
    public EntityWithCollection setCollection(List<Double> collection) {
        this.collection.clear();
        this.collection.addAll(collection);
        return this;
    }


    public List<Double> getCollection() {
        return unmodifiableList(collection);
    }

    @Observable
    public EntityWithCollection setCollection2(final List<CollectionalEntity> collection2) {
        this.collection2.clear();
        this.collection2.addAll(collection2);
        return this;
    }


    public List<CollectionalEntity> getCollection2() {
        return unmodifiableList(collection2);
    }

}
