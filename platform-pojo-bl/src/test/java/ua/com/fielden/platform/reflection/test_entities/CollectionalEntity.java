package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Test class, which represent an entity derived directly from AbstractEntity with collectional properties.
 *
 * @author 01es
 */
@KeyType(String.class)
public class CollectionalEntity extends AbstractEntity<String> {

    @IsProperty(value = String.class, linkProperty = "--stub to pass tests--")
    private final List<String> strCollectionalProperty1 = new ArrayList<>();

    @IsProperty(value = String.class, linkProperty = "--stub to pass tests--")
    private final List<String> strCollectionalProperty2 = new ArrayList<>();

    @IsProperty(value = Integer.class, linkProperty = "--stub to pass tests--")
    private final List<Integer> intCollectionalProperty = new ArrayList<>();

    protected CollectionalEntity() {
    }

    public List<Integer> getIntCollectionalProperty() {
        return unmodifiableList(intCollectionalProperty);
    }

    @Observable
    public CollectionalEntity setIntCollectionalProperty(List<Integer> intCollectionalProperty) {
        this.intCollectionalProperty.clear();
        this.intCollectionalProperty.addAll(intCollectionalProperty);
        return this;
    }


    public List<String> getStrCollectionalProperty1() {
        return unmodifiableList(strCollectionalProperty1);
    }

    @Observable
    public CollectionalEntity setStrCollectionalProperty1(List<String> strCollectionalProperty1) {
        this.strCollectionalProperty1.clear();
        this.strCollectionalProperty1.addAll(strCollectionalProperty1);
        return this;
    }


    public List<String> getStrCollectionalProperty2() {
        return unmodifiableList(strCollectionalProperty2);
    }

    @Observable
    public CollectionalEntity setStrCollectionalProperty2(List<String> strCollectionalProperty2) {
        this.strCollectionalProperty2.clear();
        this.strCollectionalProperty2.addAll(strCollectionalProperty2);
        return this;
    }

}
