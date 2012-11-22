package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

/**
 * An entity that represent a meta-information about entity centre analysis.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Analysis configuration key", desc = "Analysis configuration key")
@DefaultController(IEntityCentreAnalysisConfig.class)
@MapEntityTo("ENTITY_CENTRE_ANALYSIS_CONFIG")
public class EntityCentreAnalysisConfig extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Entity centre", desc = "Parent entity centre for analysis.")
    @MapTo("ID_ENTITY_CENTRE")
    @CompositeKeyMember(1)
    private EntityCentreConfig entityCentreConfig;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Title", desc = "Entity centre analysis configuration title.")
    @MapTo("TITLE")
    private String title;

    @Observable
    @EntityExists(EntityCentreConfig.class)
    public EntityCentreAnalysisConfig setEntityCentreConfig(final EntityCentreConfig value) {
        this.entityCentreConfig = value;
        return this;
    }

    public EntityCentreConfig getEntityCentreConfig() {
        return entityCentreConfig;
    }

    public String getTitle() {
	return title;
    }

    @Observable
    @NotNull
    public EntityCentreAnalysisConfig setTitle(final String title) {
	this.title = title;
	return this;
    }
}