package ua.com.fielden.platform.processors.test_entities;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

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