package ua.com.fielden.platform.master;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity that provides information about master to open.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Element Name")
@DescTitle("Element URI")
@CompanionObject(IMasterInfo.class)
public class MasterInfo extends AbstractEntity<String> {

    @IsProperty
    @Title("Width")
    private String width;

    @IsProperty
    @Title("Height")
    private String height;

    @IsProperty
    @Title("Width Unit")
    private String widthUnit;

    @IsProperty
    @Title(value = "Height Unit")
    private String heightUnit;

    @IsProperty
    @Title("Refresh parent centre after save?")
    private boolean shouldRefreshParentCentreAfterSave = false;

    @IsProperty
    @Title(value = "Entity Type")
    private String entityType;

    @IsProperty
    @Title(value = "Short Description", desc = "Action's short description")
    private String shortDesc;

    @IsProperty
    @Title(value = "Long Description", desc = "Action's long description")
    private String longDesc;

    @IsProperty
    @Title(value = "Relative Property Name", desc = "Property name relative to entity type that has a master")
    private String relativePropertyName;

    @IsProperty
    @Title(value = "Entity Type Title", desc = "Entity type title to show during navigation")
    private String entityTypeTitle;
    
    @IsProperty
    private Class<?> rootEntityType;

    @Observable
    public MasterInfo setRootEntityType(final Class<?> rootEntityType) {
        this.rootEntityType = rootEntityType;
        return this;
    }

    public Class<?> getRootEntityType() {
        return rootEntityType;
    }

    @Observable
    public MasterInfo setEntityTypeTitle(final String entityTypeTitle) {
        this.entityTypeTitle = entityTypeTitle;
        return this;
    }

    public String getEntityTypeTitle() {
        return entityTypeTitle;
    }

    @Observable
    public MasterInfo setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    @Observable
    public MasterInfo setShouldRefreshParentCentreAfterSave(final boolean shouldRefreshParentCentreAfterSave) {
        this.shouldRefreshParentCentreAfterSave = shouldRefreshParentCentreAfterSave;
        return this;
    }

    public boolean getShouldRefreshParentCentreAfterSave() {
        return shouldRefreshParentCentreAfterSave;
    }

    @Observable
    public MasterInfo setHeightUnit(final String heightUnit) {
        this.heightUnit = heightUnit;
        return this;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    @Observable
    public MasterInfo setWidthUnit(final String widthUnit) {
        this.widthUnit = widthUnit;
        return this;
    }

    public String getWidthUnit() {
        return widthUnit;
    }

    @Observable
    public MasterInfo setHeight(final String height) {
        this.height = height;
        return this;
    }

    public String getHeight() {
        return height;
    }

    @Observable
    public MasterInfo setWidth(final String width) {
        this.width = width;
        return this;
    }

    public String getWidth() {
        return width;
    }

    @Observable
    public MasterInfo setLongDesc(final String longDesc) {
        this.longDesc = longDesc;
        return this;
    }

    public String getLongDesc() {
        return longDesc;
    }

    @Observable
    public MasterInfo setShortDesc(final String shortDesc) {
        this.shortDesc = shortDesc;
        return this;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    @Observable
    public MasterInfo setRelativePropertyName(final String relativePropertyName) {
        this.relativePropertyName = relativePropertyName;
        return this;
    }

    public String getRelativePropertyName() {
        return relativePropertyName;
    }

    @Override
    @Observable
    public MasterInfo setDesc(String desc) {
        return super.setDesc(desc);
    }
}
