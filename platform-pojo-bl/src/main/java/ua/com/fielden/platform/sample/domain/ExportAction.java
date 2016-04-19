package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.web.action.AbstractFunEntityForDataExport;

/** 
 * An experimental action for exporting data to excel.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IExportAction.class)
public class ExportAction extends AbstractFunEntityForDataExport<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Instances to be exported", desc = "The number of first matching instances to export.")
    private Integer count;

    @Observable
    public ExportAction setCount(final Integer count) {
        this.count = count;
        return this;
    }

    public Integer getCount() {
        return count;
    }
}