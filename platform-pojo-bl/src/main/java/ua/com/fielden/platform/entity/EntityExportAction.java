package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
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
    private boolean all;

    @IsProperty
    @Title(value = "Export pages?", desc = "Export page range?")
    private boolean pageRange;

    @IsProperty
    @Title(value = "From", desc = "From page")
    private Integer fromPage;

    @IsProperty
    @Title(value = "To", desc = "To page")
    private Integer toPage;

    @IsProperty
    @Title(value = "Export selected?", desc = "Export selected entities")
    private boolean selected;

    @Observable
    public EntityExportAction setSelected(final boolean selected) {
        this.selected = selected;
        return this;
    }

    public boolean getSelected() {
        return selected;
    }

    @Observable
    public EntityExportAction setToPage(final Integer toPage) {
        this.toPage = toPage;
        return this;
    }

    public Integer getToPage() {
        return toPage;
    }

    @Observable
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