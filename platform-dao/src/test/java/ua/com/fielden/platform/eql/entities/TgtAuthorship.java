package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgtAuthorship extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title("Author")
    @CompositeKeyMember(1)
    private TgtAuthor author;

    @IsProperty
    @MapTo
    @Title("Book title")
    @CompositeKeyMember(2)
    private String title;

    @IsProperty
    @MapTo
    @Title(value = "Year", desc = "Year of publication")
    private Integer year;

    @Observable
    public TgtAuthorship setYear(final Integer year) {
        this.year = year;
        return this;
    }

    public Integer getYear() {
        return year;
    }

    public TgtAuthor getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    @EntityExists(TgtAuthor.class)
    public TgtAuthorship setAuthor(final TgtAuthor author) {
        this.author = author;
        return this;
    }

    @Observable
    public TgtAuthorship setTitle(final String title) {
        this.title = title;
        return this;
    }
}