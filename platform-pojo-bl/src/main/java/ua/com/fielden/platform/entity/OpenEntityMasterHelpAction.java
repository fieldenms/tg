package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
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
public class OpenEntityMasterHelpAction extends AbstractEntity<NoKey> {

    @IsProperty
    @Title(value = "Entity Type", desc = "Entity Type Name")
    private String entityType;

    @IsProperty
    @Title(value = "Entity Title")
    private String entityTitle;

    @IsProperty
    @Title("Hyperlink")
    private Hyperlink link;

    @Observable
    public OpenEntityMasterHelpAction setLink(final Hyperlink link) {
        this.link = link;
        return this;
    }

    public Hyperlink getLink() {
        return link;
    }

    protected OpenEntityMasterHelpAction() {
        setKey(NO_KEY);
    }

    @Observable
    public OpenEntityMasterHelpAction setEntityTitle(final String entityTitle) {
        this.entityTitle = entityTitle;
        return this;
    }

    public String getEntityTitle() {
        return entityTitle;
    }

    @Observable
    public OpenEntityMasterHelpAction setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }
}
