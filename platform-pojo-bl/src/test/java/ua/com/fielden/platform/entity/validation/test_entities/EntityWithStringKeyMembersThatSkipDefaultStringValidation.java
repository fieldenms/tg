package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipDefaultStringKeyMemberValidation;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;
import ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator;

/**
 * Test entity type with multiple key members of type {@link String} and various skip annotations to relax the default string key validation.
 *
 * @author TG Team
 */
@KeyType(DynamicEntityKey.class)
public class EntityWithStringKeyMembersThatSkipDefaultStringValidation extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @SkipDefaultStringKeyMemberValidation
    private String member1;

    @IsProperty
    @CompositeKeyMember(2)
    @SkipDefaultStringKeyMemberValidation(RestrictCommasValidator.class)
    private String member2;
    
    @IsProperty
    @CompositeKeyMember(3)
    @SkipDefaultStringKeyMemberValidation({RestrictCommasValidator.class, RestrictExtraWhitespaceValidator.class})
    private String member3;

    @IsProperty
    @CompositeKeyMember(4)
    @SkipDefaultStringKeyMemberValidation(RestrictNonPrintableCharactersValidator.class)
    private String member4;

    @Observable
    public EntityWithStringKeyMembersThatSkipDefaultStringValidation setMember4(final String member4) {
        this.member4 = member4;
        return this;
    }

    public String getMember4() {
        return member4;
    }

    @Observable
    public EntityWithStringKeyMembersThatSkipDefaultStringValidation setMember3(final String member3) {
        this.member3 = member3;
        return this;
    }

    public String getMember3() {
        return member3;
    }

    @Observable
    public EntityWithStringKeyMembersThatSkipDefaultStringValidation setMember2(final String member2) {
        this.member2 = member2;
        return this;
    }

    public String getMember2() {
        return member2;
    }

    @Observable
    public EntityWithStringKeyMembersThatSkipDefaultStringValidation setMember1(final String name) {
        this.member1 = name;
        return this;
    }

    public String getMember1() {
        return member1;
    }

}