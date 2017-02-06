package ua.com.fielden.platform.web.action;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * A base functional entity to be used for implementing any data export actions such as export to MS Excel, PDF etc.
 * 
 * @author TG Team
 *
 */
public abstract class AbstractFunEntityForDataExport<K extends Comparable<?>> extends AbstractFunctionalEntityWithCentreContext<K> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "MIME", desc = "File MIME Type")
    private String mime; // application/pdf, application/vnd.ms-excel, text/plain, text/html

    @IsProperty
    @MapTo
    @Title(value = "File Name", desc = "The name of file for the data to be saved into")
    private String fileName;

    @IsProperty
    @Title(value = "Data", desc = "Raw binary data that needs to be persisted.")
    private byte[] data;

    @Observable
    public AbstractFunEntityForDataExport<K> setFileName(final String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }
    
    @Observable
    public AbstractFunEntityForDataExport<K> setMime(final String mime) {
        this.mime = mime;
        return this;
    }

    public String getMime() {
        return mime;
    }

    @Observable
    public AbstractFunEntityForDataExport<K> setData(final byte[] data) {
        this.data = data;
        return this;
    }

    public byte[] getData() {
        return data;
    }

}