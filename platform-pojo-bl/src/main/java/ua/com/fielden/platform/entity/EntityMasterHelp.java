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
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.utils.Pair;

/**
 * Entity that represents configuration for entity master help. It maps entity type and help document vi the hyperlink.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Entity Type")
@CompanionObject(EntityMasterHelpCo.class)
@MapEntityTo
public class EntityMasterHelp extends AbstractPersistentEntity<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityMasterHelp.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Entity Type", desc = "Entity Type Name")
    @CompositeKeyMember(1)
    private String entityType;

    @IsProperty
    @MapTo
    @Title(value = "Help Link", desc = "Hyperlink on help document")
    @Required
    private Hyperlink help;

    @Observable
    public EntityMasterHelp setHelp(final Hyperlink help) {
        this.help = help;
        return this;
    }

    public Hyperlink getHelp() {
        return help;
    }

    @Observable
    public EntityMasterHelp setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }
}
