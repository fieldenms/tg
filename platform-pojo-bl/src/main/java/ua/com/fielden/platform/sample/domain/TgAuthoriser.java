package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Authoriser")
@CompanionObject(ITgAuthoriser.class)
@MapEntityTo
public class TgAuthoriser extends ActivatableAbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Person", desc = "Person who is regarded as an authoriser")
    @CompositeKeyMember(1)
    private TgPerson person;

    @IsProperty
    @MapTo
    @Title(value = "Category", desc = "Desc")
    private TgCategory category;

    @Observable
    public TgAuthoriser setCategory(final TgCategory category) {
        this.category = category;
        return this;
    }

    public TgCategory getCategory() {
        return category;
    }


    @Observable
    public TgAuthoriser setPerson(final TgPerson person) {
        this.person = person;
        return this;
    }

    public TgPerson getPerson() {
        return person;
    }

    @Override
    @Observable
    public TgAuthoriser setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}
