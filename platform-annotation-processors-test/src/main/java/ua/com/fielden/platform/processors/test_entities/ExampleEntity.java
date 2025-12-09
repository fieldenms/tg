package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An example entity for testing purposes.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Description")
public class ExampleEntity extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(ExampleEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private Integer prop1;

    @IsProperty
    @MapTo
    @Title(value = "Flag value (yes/no)")
    private boolean flag;

    @IsProperty(ExampleEntity.class)
    @Title(value = "Collection of entities of this type")
    private final Set<ExampleEntity> collection = new LinkedHashSet<ExampleEntity>();

    @IsProperty
    @MapTo
    @Title("A property that exists to manifest a common union property")
    private String common1;

    @IsProperty
    @MapTo
    @Title("A property that exists to manifest a common union property")
    private EntityWithOrdinaryProps common2;

    public EntityWithOrdinaryProps getCommon2() {
        return common2;
    }

    @Observable
    public ExampleEntity setCommon2(final EntityWithOrdinaryProps common2) {
        this.common2 = common2;
        return this;
    }

    public String getCommon1() {
        return common1;
    }

    @Observable
    public ExampleEntity setCommon1(final String common1) {
        this.common1 = common1;
        return this;
    }

    @Observable
    protected ExampleEntity setCollection(final Set<ExampleEntity> name) {
        this.collection.clear();
        this.collection.addAll(name);
        return this;
    }

    public Set<ExampleEntity> getCollection() {
        return Collections.unmodifiableSet(collection);
    }

    @Observable
    public ExampleEntity setFlag(final boolean flag) {
        this.flag = flag;
        return this;
    }

    public boolean isFlag() {
        return flag;
    }

    @Observable
    public ExampleEntity setProp1(final Integer prop1) {
        this.prop1 = prop1;
        return this;
    }

    public Integer getProp1() {
        return prop1;
    }

}
