package ua.com.fielden.platform.sample.domain.stream_processors;

import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * An demo entity that both provides an input stream for processing and represent the result of that processing as implemented in its companion's method <code>save</code>.
 * <p>
 * An important part of this entity is that it extends {@link AbstractEntityWithInputStream}.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IDumpCsvTxtProcessor.class)
public class DumpCsvTxtProcessor extends AbstractEntityWithInputStream<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Integer noOfProcessedLines;

    @Observable
    public DumpCsvTxtProcessor setNoOfProcessedLines(final Integer noOfProcessedLines) {
        this.noOfProcessedLines = noOfProcessedLines;
        return this;
    }

    public Integer getNoOfProcessedLines() {
        return noOfProcessedLines;
    }
    
}