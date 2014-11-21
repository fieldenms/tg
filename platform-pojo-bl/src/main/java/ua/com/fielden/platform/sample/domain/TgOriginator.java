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
@KeyTitle("Originator")
@CompanionObject(ITgOriginator.class)
@MapEntityTo
public class TgOriginator extends ActivatableAbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Person", desc = "Person who is regarded as an originator")
    @CompositeKeyMember(1)
    private TgPerson person;

    @IsProperty
    @MapTo
    @Title(value = "Assistant", desc = "Originators assistant")
    private TgPerson assistant;

    @Observable
    public TgOriginator setAssistant(final TgPerson assistant) {
        this.assistant = assistant;
        return this;
    }

    public TgPerson getAssistant() {
        return assistant;
    }

    @Observable
    public TgOriginator setPerson(final TgPerson person) {
        this.person = person;
        return this;
    }

    public TgPerson getPerson() {
        return person;
    }

    @Override
    @Observable
    public TgOriginator setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}
