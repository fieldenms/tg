package ua.com.fielden.platform.serialisation.jackson.entities;

import jakarta.annotation.Nonnull;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.DefaultValidatorForValueTypeWithValidation;
import ua.com.fielden.platform.entity.validation.UnhappyValidator;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.types.RichText;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 */
@KeyType(String.class)
@DescTitle("Description")
public class EntityWithRichText extends AbstractEntity<String> {

    public enum Property implements IConvertableToPath {
        richText, unhappyRichText;

        @Override
        @Nonnull
        public String toPath() {
            return name();
        }
    }

    @IsProperty
    @MapTo
    @Title("Title")
    private RichText richText;

    @IsProperty
    @MapTo
    @Title("Unhappy RichText")
    @BeforeChange({ @Handler(UnhappyValidator.class), @Handler(DefaultValidatorForValueTypeWithValidation.class) })
    private RichText unhappyRichText;

    public RichText getUnhappyRichText() {
        return unhappyRichText;
    }

    @Observable
    public EntityWithRichText setUnhappyRichText(final RichText unhappyRichText) {
        this.unhappyRichText = unhappyRichText;
        return this;
    }

    @Observable
    public EntityWithRichText setRichText(final RichText richText) {
        this.richText = richText;
        return this;
    }

    public RichText getRichText() {
        return richText;
    }

}
