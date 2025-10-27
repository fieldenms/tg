package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * Union entity type for web tests.
 * 
 * @author TG Team
 *
 */
@EntityTitle("TG Union")
@KeyTitle("Entity With Union Key")
// @DescTitle should not be used here -- description is either common property from union types or can be present individually there; description cannot be present on union type itself
@CompanionObject(TgUnionCo.class)
public class TgUnion extends AbstractUnionEntity {

    @IsProperty
    @MapTo
    @Title("Union 1")
    private TgUnionType1 union1;

    @IsProperty
    @MapTo
    @Title("Union 2")
    private TgUnionType2 union2;

    @Observable
    public TgUnion setUnion2(final TgUnionType2 union2) {
        this.union2 = union2;
        return this;
    }

    public TgUnionType2 getUnion2() {
        return union2;
    }

    @Observable
    public TgUnion setUnion1(final TgUnionType1 union1) {
        this.union1 = union1;
        return this;
    }

    public TgUnionType1 getUnion1() {
        return union1;
    }

}