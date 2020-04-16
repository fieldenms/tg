package ua.com.fielden.platform.master;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle("Element Name")
@DescTitle("Element URI")
public class MasterInfo extends AbstractEntity<String> {

    @IsProperty
    @Title("Width")
    private String width;

    @IsProperty
    @Title("Hegiht")
    private String height;

    @IsProperty
    @Title("Width Unit")
    private String widthUnit;

    @IsProperty
    @Title(value = "Height Unit", desc = "Desc")
    private String heightUnit;

    @IsProperty
    @Title(value = "Short Description", desc = "Action's short description")
    private String shortDescription;

    @IsProperty
    @Title(value = "Long Description", desc = "Action's long description")
    private String longDescription;

    @IsProperty
    @Title("Refresh parent centre after save?")
    private boolean shouldRefreshParentCentreAfterSave;

    @IsProperty
    @Title("Require Selection Criteria")
    private String requireSelectionCriteria;

    @IsProperty
    @Title("Require Selected Entities")
    private String requireSelectedEntities;

    @IsProperty
    @Title("Require Master Entity")
    private String requireMasterEntity;

    @Observable
    public MasterInfo setRequireMasterEntity(final String requireMasterEntity) {
        this.requireMasterEntity = requireMasterEntity;
        return this;
    }

    public String getRequireMasterEntity() {
        return requireMasterEntity;
    }

    @Observable
    public MasterInfo setRequireSelectedEntities(final String requireSelectedEntities) {
        this.requireSelectedEntities = requireSelectedEntities;
        return this;
    }

    public String getRequireSelectedEntities() {
        return requireSelectedEntities;
    }

    @Observable
    public MasterInfo setRequireSelectionCriteria(final String requireSelectionCriteria) {
        this.requireSelectionCriteria = requireSelectionCriteria;
        return this;
    }

    public String getRequireSelectionCriteria() {
        return requireSelectionCriteria;
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
    public MasterInfo setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
        return this;
    }

    public String getLongDescription() {
        return longDescription;
    }

    @Observable
    public MasterInfo setShortDescription(final String shortDescription) {
        this.shortDescription = shortDescription;
        return this;
    }

    public String getShortDescription() {
        return shortDescription;
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
}
