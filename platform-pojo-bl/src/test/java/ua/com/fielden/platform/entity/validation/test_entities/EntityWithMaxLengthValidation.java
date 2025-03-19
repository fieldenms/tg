/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.IntParam;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.RichText;

/**
 * Entity for testing of validator {@link MaxLengthValidator}.
 * 
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithMaxLengthValidation extends AbstractEntity<String> {

    @IsProperty
    @BeforeChange(@Handler(value = MaxLengthValidator.class, integer = @IntParam(name = "limit", value = 5)))
    private String propWithLimit;

    @IsProperty(length = 3)
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String propWithLength;

    @IsProperty(length = 3)
    @BeforeChange(@Handler(value = MaxLengthValidator.class, integer = @IntParam(name = "limit", value = 5)))
    private String propWithSmallerLengthAndGreaterLimit;
    
    @IsProperty(length = 5)
    @BeforeChange(@Handler(value = MaxLengthValidator.class, integer = @IntParam(name = "limit", value = 3)))
    private String propWithGreaterLengthAndSmallerLimit;

    @IsProperty
    @BeforeChange(@Handler(MaxLengthValidator.class)) // invalid declaration -- neither IsProperty.lenght nor MaxLengthValidator.limit are specified
    private String propWithInvalidDeclaration;
    
    @IsProperty(length = 30)
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private Hyperlink propHyperlink;

    @IsProperty(length = 27)
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private RichText richText;

    @IsProperty(length = 27)
    private String stringPropWithoutExplicitMaxLengthValidator;

    @IsProperty(length = 27)
    private RichText richTextPropWithoutExplicitMaxLengthValidator;

    @IsProperty(length = 27)
    private Hyperlink hyperlinkPropWithoutExplicitMaxLengthValidator;

    public Hyperlink getHyperlinkPropWithoutExplicitMaxLengthValidator() {
        return hyperlinkPropWithoutExplicitMaxLengthValidator;
    }

    @Observable
    public EntityWithMaxLengthValidation setHyperlinkPropWithoutExplicitMaxLengthValidator(final Hyperlink hyperlinkPropWithoutExplicitMaxLengthValidator) {
        this.hyperlinkPropWithoutExplicitMaxLengthValidator = hyperlinkPropWithoutExplicitMaxLengthValidator;
        return this;
    }

    @IsProperty
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private Integer intProp;

    public RichText getRichTextPropWithoutExplicitMaxLengthValidator() {
        return richTextPropWithoutExplicitMaxLengthValidator;
    }

    @Observable
    public EntityWithMaxLengthValidation setRichTextPropWithoutExplicitMaxLengthValidator(final RichText richTextPropWithoutExplicitMaxLengthValidator) {
        this.richTextPropWithoutExplicitMaxLengthValidator = richTextPropWithoutExplicitMaxLengthValidator;
        return this;
    }

    @Observable
    public EntityWithMaxLengthValidation setStringPropWithoutExplicitMaxLengthValidator(final String stringPropWithoutExplicitMaxLengthValidator) {
        this.stringPropWithoutExplicitMaxLengthValidator = stringPropWithoutExplicitMaxLengthValidator;
        return this;
    }

    public String getStringPropWithoutExplicitMaxLengthValidator() {
        return stringPropWithoutExplicitMaxLengthValidator;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public EntityWithMaxLengthValidation setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public RichText getRichText() {
        return richText;
    }

    @Observable
    public EntityWithMaxLengthValidation setRichText(final RichText richText) {
        this.richText = richText;
        return this;
    }

    @Observable
    public EntityWithMaxLengthValidation setPropHyperlink(final Hyperlink propHyperlink) {
        this.propHyperlink = propHyperlink;
        return this;
    }

    public Hyperlink getPropHyperlink() {
        return propHyperlink;
    }

    @Observable
    public EntityWithMaxLengthValidation setPropWithSmallerLengthAndGreaterLimit(final String propWithLengthAndLimit) {
        this.propWithSmallerLengthAndGreaterLimit = propWithLengthAndLimit;
        return this;
    }

    public String getPropWithSmallerLengthAndGreaterLimit() {
        return propWithSmallerLengthAndGreaterLimit;
    }
    
    @Observable
    public EntityWithMaxLengthValidation setPropWithLength(final String propWithLength) {
        this.propWithLength = propWithLength;
        return this;
    }

    public String getPropWithLength() {
        return propWithLength;
    }

    @Observable
    public EntityWithMaxLengthValidation setPropWithInvalidDeclaration(final String value) {
        this.propWithInvalidDeclaration = value;
        return this;
    }

    public String getPropWithInvalidDeclaration() {
        return propWithInvalidDeclaration;
    }

    @Observable
    public EntityWithMaxLengthValidation setPropWithLimit(final String name) {
        this.propWithLimit = name;
        return this;
    }

    public String getPropWithLimit() {
        return propWithLimit;
    }

    @Observable
    public EntityWithMaxLengthValidation setPropWithGreaterLengthAndSmallerLimit(final String propWithLargerLengthAndSmallerLimit) {
        this.propWithGreaterLengthAndSmallerLimit = propWithLargerLengthAndSmallerLimit;
        return this;
    }

    public String getPropWithGreaterLengthAndSmallerLimit() {
        return propWithGreaterLengthAndSmallerLimit;
    }

}
