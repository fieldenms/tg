package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.utils.Pair;

/**
 * A persistent entity-action that represents configuration for a user-definable help, which can be associated with either an Entity Master and an Entity Centre.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Reference Element Type")
@CompanionObject(UserDefinableHelpCo.class)
@MapEntityTo
public class UserDefinableHelp extends AbstractFunctionalEntityWithCentreContext<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(UserDefinableHelp.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty(length = 255)
    @MapTo
    @Title(value = "Reference Element Type", desc = "The type of element that associates the view with help")
    @CompositeKeyMember(1)
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String referenceElement;

    @IsProperty(length = 2048)
    @MapTo
    @Title(value = "Help Link", desc = "A hyperlink to a help document (e.g., a wiki page)")
    @Required
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private Hyperlink help;

    /**
     * Property <code>skipUi</code> controls visibility of the Help entity master, which depends on the way this entity-action is invoked.
     */
    @IsProperty
    private boolean skipUi = false;

    @Observable
    public UserDefinableHelp setSkipUi(final boolean skipUi) {
        this.skipUi = skipUi;
        return this;
    }

    public boolean isSkipUi() {
        return skipUi;
    }

    @Observable
    public UserDefinableHelp setHelp(final Hyperlink help) {
        this.help = help;
        return this;
    }

    public Hyperlink getHelp() {
        return help;
    }

    @Observable
    public UserDefinableHelp setReferenceElement(final String entityType) {
        this.referenceElement = entityType;
        return this;
    }

    public String getReferenceElement() {
        return referenceElement;
    }

}