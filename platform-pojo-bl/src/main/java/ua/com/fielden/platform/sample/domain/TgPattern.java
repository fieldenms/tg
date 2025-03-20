package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Pattern")
@CompanionObject(TgPatternCo.class)
@MapEntityTo
public class TgPattern extends AbstractEntity<DynamicEntityKey> {

    public static final String PATTERN = "pattern";

    @IsProperty
    @MapTo
    @Title(value = "Pattern")
    @CompositeKeyMember(1)
    @SkipDefaultStringKeyMemberValidation
    private String pattern;

    @Observable
    public TgPattern setPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

}
