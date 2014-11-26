package ua.com.fielden.platform.gis.gps;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.security.user.User;

/**
 * One-2-Many entity object.
 *
 * @author Developers
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Прив'язка машини з модулем", desc = "Прив'язка машини з модулем")
@DescTitle(value = "Коментар", desc = "Додатковий коментар щодо проведеної асоціації машини з модулем")
@EntityTitle(value = "Прив'язка машини з модулем", desc = "Прив'язка машини з модулем")
// TODO do not forget to provide companion object in its descendants -- @CompanionObject(IMachineModuleAssociation.class)
@MapEntityTo
public abstract class AbstractAvlMachineModuleTemporalAssociation<MESSAGE extends AbstractAvlMessage, MACHINE extends AbstractAvlMachine<MESSAGE>, MODULE extends AbstractAvlModule> extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 6689517578526230679L;

    //    @IsProperty
    //    @Title(value = "Machine", desc = "Machine")
    //    @MapTo
    //    @CompositeKeyMember(1)
    //    private MACHINE machine;
    //
    //    @IsProperty
    //    @MapTo
    //    @Title(value = "Module", desc = "Module associated with a machine")
    //    @CompositeKeyMember(2)
    //    private MODULE module;

    @IsProperty
    @MapTo
    @Dependent("to")
    @Title(value = "Від", desc = "Дата, починаючи з якої модуль був прив'язаний до машини")
    @CompositeKeyMember(3)
    private Date from;

    @IsProperty
    @MapTo
    @Dependent("from")
    @Title(value = "До", desc = "Дата до якої модуль був прив'язаний до машини")
    private Date to;

    @IsProperty(assignBeforeSave = true)
    @MapTo
    @Title(value = "Дата створення", desc = "Дата створення асоціації")
    private Date created;

    @IsProperty
    @MapTo
    @Title(value = "Дата зміни", desc = "Дата зміни асоціації")
    private Date changed;

    @IsProperty(assignBeforeSave = true)
    @MapTo
    @Title(value = "Прив'язувач", desc = "Користувач, що провів асоціацію Машини з Модулем в часі")
    @Readonly
    private User createdBy;

    @IsProperty
    @MapTo
    @Title(value = "Відв'язувач", desc = "Користувач, що змінив асоціацію Машини з Модулем в часі")
    // @TransactionUser
    // @Required
    @Readonly
    private User changedBy;

    @Observable
    @EntityExists(User.class)
    public AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setChangedBy(final User changedBy) {
        this.changedBy = changedBy;
        return this;
    }

    public User getChangedBy() {
        return createdBy;
    }

    @Observable
    @EntityExists(User.class)
    public AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setCreatedBy(final User createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    @Observable
    public AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setChanged(final Date changed) {
        this.changed = changed;
        return this;
    }

    public Date getChanged() {
        return changed;
    }

    @Observable
    public AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setCreated(final Date created) {
        this.created = created;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    @Observable
    @LeProperty("to")
    public AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setFrom(final Date from) {
        this.from = from;
        return this;
    }

    public Date getFrom() {
        return from;
    }

    @Observable
    @GeProperty("from")
    public AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setTo(final Date to) {
        this.to = to;
        return this;
    }

    public Date getTo() {
        return to;
    }

    protected abstract AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setMachine(final MACHINE value);

    public abstract MACHINE getMachine();

    protected abstract AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setModule(final MODULE module);

    public abstract MODULE getModule();
}