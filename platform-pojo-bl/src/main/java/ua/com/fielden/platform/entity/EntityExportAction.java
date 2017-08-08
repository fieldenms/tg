package ua.com.fielden.platform.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.action.AbstractFunEntityForDataExport;

/**
 * A functional entity that represents an action for exporting entities to Excel.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Export", desc = "Export data into file")
@CompanionObject(IEntityExportAction.class)
public class EntityExportAction extends AbstractFunEntityForDataExport<String> {
    public static final String PROP_EXPORT_ALL = "exportAll";
    public static final String PROP_EXPORT_TOP = "exportTop";
    public static final String PROP_EXPORT_SELECTED = "exportSelected";
    public static final String PROP_NUMBER = "number";
    public static final Set<String> EXPORT_OPTION_PROPERTIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(PROP_EXPORT_ALL, PROP_EXPORT_TOP, PROP_EXPORT_SELECTED)));
    
    @IsProperty
    @Title(value = "Export all?", 
           desc = "Should be used in cases where all matching entities across all pages need to be exported.")
    @AfterChange(ExportActionHandler.class)
    private boolean exportAll;

    @IsProperty
    @Title(value = "Export top?", 
           desc = "Should be used in cases where the specified number of the top matching entities need to be exported."
                   + "If there are less mathing entities than the number specified then only those get exported.")
    @AfterChange(ExportActionHandler.class)
    private boolean exportTop;

    @IsProperty
    @Title(value = "Number", desc = "The number of top matching entities to be exported.")
    private Integer number;

    @IsProperty
    @Title(value = "Export selected?", desc = "Export selected entities")
    @AfterChange(ExportActionHandler.class)
    private boolean exportSelected;
    
    @IsProperty
    @Title("Context Holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty(Long.class)
    @Title("Selected Entity IDs")
    private Set<Long> selectedEntityIds = new HashSet<Long>();

    @Observable
    protected EntityExportAction setSelectedEntityIds(final Set<Long> selectedEntityIds) {
        this.selectedEntityIds.clear();
        this.selectedEntityIds.addAll(selectedEntityIds);
        return this;
    }

    public Set<Long> getSelectedEntityIds() {
        return Collections.unmodifiableSet(selectedEntityIds);
    }

    @Observable
    public EntityExportAction setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }

    public CentreContextHolder getCentreContextHolder() {
        return centreContextHolder;
    }
    @Observable
    public EntityExportAction setExportSelected(final boolean exportSelected) {
        this.exportSelected = exportSelected;
        return this;
    }

    public boolean isExportSelected() {
        return exportSelected;
    }

    @Observable
    @GreaterOrEqual(1)
    public EntityExportAction setNumber(final Integer number) {
        this.number = number;
        return this;
    }

    public Integer getNumber() {
        return number;
    }

    @Observable
    public EntityExportAction setExportTop(final boolean exportTop) {
        this.exportTop = exportTop;
        return this;
    }

    public boolean isExportTop() {
        return exportTop;
    }

    @Observable
    public EntityExportAction setExportAll(final boolean exportAll) {
        this.exportAll = exportAll;
        return this;
    }

    public boolean isExportAll() {
        return exportAll;
    }

    @Override
    protected Result validate() {
        final Result superResult = super.validate();

        for (final String property : EXPORT_OPTION_PROPERTIES) {
            if ((Boolean) get(property)) {
                return superResult;
            }
        }

        return superResult.isSuccessful() ? Result.failure("One of the export options must be selected.") : superResult;
    }
}