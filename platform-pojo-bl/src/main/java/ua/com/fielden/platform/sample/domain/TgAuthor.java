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
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyTitle("Author")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@DefaultController(ITgAuthor.class)
public class TgAuthor extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty @MapTo @Title("Name")
    @CompositeKeyMember(1)
    private TgPersonName name;

    @IsProperty @MapTo @Title("Surname")
    @CompositeKeyMember(2)
    private String surname;

    public TgPersonName getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Observable
    @EntityExists(TgPersonName.class)
    public TgAuthor setName(final TgPersonName name) {
        this.name = name;
        return this;
    }

    @Observable
    public TgAuthor setSurname(final String surname) {
        this.surname = surname;
        return this;
    }
}