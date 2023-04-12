package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Hyperlink;

/**
 * Action to open entity master help.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(OpenEntityMasterHelpActionCo.class)
public class OpenEntityMasterHelpAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Entity Type", desc = "Entity Type Name")
    private String entityType;

    @IsProperty
    @Title(value = "Help Link", desc = "Hyperlink on help document")
    private Hyperlink help;

    /**
     * Property <code>skipUi</code> controls visibility of Help entity master.
     */
    @IsProperty
    private boolean skipUi = false;

    protected OpenEntityMasterHelpAction() {
        setKey(NO_KEY);
    }

    @Observable
    public OpenEntityMasterHelpAction setSkipUi(final boolean skipUi) {
        this.skipUi = skipUi;
        return this;
    }

    public boolean isSkipUi() {
        return skipUi;
    }

    @Observable
    public OpenEntityMasterHelpAction setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    @Observable
    public OpenEntityMasterHelpAction setHelp(final Hyperlink help) {
        this.help = help;
        return this;
    }

    public Hyperlink getHelp() {
        return help;
    }
}
