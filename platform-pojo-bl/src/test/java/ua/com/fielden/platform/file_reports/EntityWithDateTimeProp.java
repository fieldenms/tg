package ua.com.fielden.platform.file_reports;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(String.class)
public class EntityWithDateTimeProp extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    private DateTime dateTimeProp;

    public DateTime getDateTimeProp() {
        return dateTimeProp;
    }

    @Observable
    public void setDateTimeProp(final DateTime dateTimeProp) {
        this.dateTimeProp = dateTimeProp;
    }
}
