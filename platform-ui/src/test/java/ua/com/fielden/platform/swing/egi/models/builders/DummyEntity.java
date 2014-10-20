package ua.com.fielden.platform.swing.egi.models.builders;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Dummy entity for example/testing
 *
 * @author Yura
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Dummy entity number")
@DescTitle(value = "Description", desc = "Dummy entity description")
public class DummyEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Dummy entity 2 ref", desc = "Dummy entity 2 string reference")
    private String dummyEntity2Ref;

    @IsProperty
    @Title(value = "Dummy entity 2", desc = "Dummy entity 2 direct reference")
    private DummyEntity2 dummyEntity2;

    protected DummyEntity() {
    }

    public DummyEntity(final String key, final String desc) {
        super(null, key, desc);
    }

    public String getDummyEntity2Ref() {
        return dummyEntity2Ref;
    }

    @Observable
    public DummyEntity setDummyEntity2Ref(final String dummyEntity2Ref) {
        this.dummyEntity2Ref = dummyEntity2Ref;
        return this;
    }

    public DummyEntity2 getDummyEntity2() {
        return dummyEntity2;
    }

    @Observable
    public DummyEntity setDummyEntity2(final DummyEntity2 dummyEntity2) {
        this.dummyEntity2 = dummyEntity2;
        return this;
    }

}
