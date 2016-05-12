package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.web.action.AbstractFunEntityForDataExport;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Export", desc = "Export data into file")
@CompanionObject(IEntityExportAction.class)
public class EntityExportAction extends AbstractFunEntityForDataExport<String> {
    private static final long serialVersionUID = 3228002036372799747L;

    @IsProperty
    @Title(value = "Export all?", desc = "Export all entities?")
    @BeforeChange(@Handler(ExportActionOptionValidator.class))
    @AfterChange(ExportActionHandler.class)
    private boolean all = true;

    @IsProperty
    @Title(value = "Export pages?", desc = "Export page range?")
    @BeforeChange(@Handler(ExportActionOptionValidator.class))
    @AfterChange(ExportActionHandler.class)
    private boolean pageRange;

    @IsProperty
    @Title(value = "From", desc = "From page")
    @Dependent("toPage")
    private Integer fromPage;

    @IsProperty
    @Title(value = "To", desc = "To page")
    @Dependent("fromPage")
    private Integer toPage;

    @IsProperty
    @Title(value = "Export selected?", desc = "Export selected entities")
    @BeforeChange(@Handler(ExportActionOptionValidator.class))
    @AfterChange(ExportActionHandler.class)
    private boolean selected;

    @IsProperty
    @Title(value = "Page capacity", desc = "The number of entities on page")
    private Integer pageCapacity;

    @Observable
    public EntityExportAction setPageCapacity(final Integer pageCapacity) {
        this.pageCapacity = pageCapacity;
        return this;
    }

    public Integer getPageCapacity() {
        return pageCapacity;
    }

    @Observable
    public EntityExportAction setSelected(final boolean selected) {
        this.selected = selected;
        return this;
    }

    public boolean getSelected() {
        return selected;
    }

    @Observable
    @GeProperty("fromPage")
    @GreaterOrEqual(1)
    public EntityExportAction setToPage(final Integer toPage) {
        this.toPage = toPage;
        return this;
    }

    public Integer getToPage() {
        return toPage;
    }

    @Observable
    @LeProperty("toPage")
    @GreaterOrEqual(1)
    public EntityExportAction setFromPage(final Integer fromPage) {
        this.fromPage = fromPage;
        return this;
    }

    public Integer getFromPage() {
        return fromPage;
    }

    @Observable
    public EntityExportAction setPageRange(final boolean pageRange) {
        this.pageRange = pageRange;
        return this;
    }

    public boolean getPageRange() {
        return pageRange;
    }

    @Observable
    public EntityExportAction setAll(final boolean all) {
        this.all = all;
        return this;
    }

    public boolean getAll() {
        return all;
    }

}