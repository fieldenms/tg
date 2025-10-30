package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(UnionEntityDetailsCo.class)
public class UnionEntityDetails extends AbstractEntity<DynamicEntityKey> {

    public enum Property implements IConvertableToPath {
        serial, union;

        @Override public String toPath() { return name(); }
    }

    @IsProperty
    @MapTo
    @Title(value = "Serial number")
    @CompositeKeyMember(1)
    private String serial;

    @IsProperty
    @MapTo
    @Title(value = "Union")
    @CompositeKeyMember(2)
    private UnionEntity union;

    @Observable
    public UnionEntityDetails setUnion(final UnionEntity union) {
        this.union = union;
        return this;
    }

    public UnionEntity getUnion() {
        return union;
    }

    @Observable
    public UnionEntityDetails setSerial(final String serial) {
        this.serial = serial;
        return this;
    }

    public String getSerial() {
        return serial;
    }


}
