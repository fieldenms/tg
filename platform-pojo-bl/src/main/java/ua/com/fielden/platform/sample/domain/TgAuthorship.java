package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyTitle("Authorship")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgAuthorship.class)
public class TgAuthorship extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title("Author")
    @CompositeKeyMember(1)
    private TgAuthor author;

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
    public TgAuthorship setYear(final Integer year) {
        this.year = year;
        return this;
    }

    public Integer getYear() {
        return year;
    }

    public TgAuthor getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    @EntityExists(TgAuthor.class)
    public TgAuthorship setAuthor(final TgAuthor author) {
        this.author = author;
        return this;
    }

    @Observable
    public TgAuthorship setTitle(final String title) {
        this.title = title;
        return this;
    }
}