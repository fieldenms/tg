package ua.com.fielden.platform.gis.gps;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.TransactionEntity;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

@KeyType(String.class)
@EntityTitle(value = "Машина", desc = "Автомобіль, або спецтехніка.")
@KeyTitle(value = "Номерний знак", desc = "Реєстраційний знак машини.")
@DescTitle(value = "Опис", desc = "Тут можна вказати описову інформацію про машину.")
@MapEntityTo
//TODO do not forget to provide companion object in its descendants -- @CompanionObject(IMachine.class)
@TransactionEntity("lastMessage.packetReceived") // this is marker only -- the actual "delta" query uses special MachineMonitor resource
public class AbstractAvlMachine <T extends AbstractAvlMessage> extends AbstractEntity<String> {
    private static final long serialVersionUID = -50301420153223995L;

    @IsProperty
    @MapTo
    @Title(value = "У використанні?", desc = "Вказує, чи машина є у використанні.")
    @Invisible
    private boolean deleted;

    @IsProperty
    @MapTo
    @Title(value = "Час вилучення", desc = "Час коли машина була вилучена із використання.")
    @Invisible
    private Date deletedTime;

    @IsProperty
    @MapTo
    @Title(value = "Enabled", desc = "Enabled")
    @Invisible
    private boolean enabled;

    @IsProperty
    @MapTo
    @Title(value = "Object code", desc = "Object code")
    @Invisible
    private String objectCode;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Останнє GPS повідомлення", desc = "Містить інформацію про останнє GPS повідомлення, отримане від GPS модуля.")
    private T lastMessage;
    private static ExpressionModel lastMessage_ = expr().val(null).model();
	    // expr().model(select(Message.class).where().prop(Message.MACHINE_PROP_ALIAS).eq().extProp("id").and().//
	    // notExists(select(Message.class).where().prop(Message.MACHINE_PROP_ALIAS).eq().extProp(Message.MACHINE_PROP_ALIAS).and().prop("gpsTime").gt().extProp("gpsTime").model()).model()).model();

    // TODO @IsProperty(value = Message.class, linkProperty="machine")
    @Title(value = "Останні GPS повідомлення", desc = "Містить інформацію про останні GPS повідомлення, починаючи з деякого часу")
    private List<T> lastMessages = new ArrayList<T>();

    @Observable
    protected AbstractAvlMachine<T> setLastMessages(final List<T> lastMessages) {
	this.lastMessages.clear();
	this.lastMessages.addAll(lastMessages);
	return this;
    }

    public List<T> getLastMessages() {
	return Collections.unmodifiableList(lastMessages);
    }

    @Observable
    public AbstractAvlMachine<T> setLastMessage(final T lastMessage) {
	this.lastMessage = lastMessage;
	return this;
    }

    public T getLastMessage() {
	return lastMessage;
    }

    @Override
    @NotNull
    @Observable
    public AbstractAvlMachine<T>  setKey(final String key) {
	super.setKey(key);
	return this;
    }

    @Observable
    public AbstractAvlMachine<T>  setObjectCode(final String objectCode) {
	this.objectCode = objectCode;
	return this;
    }

    public String getObjectCode() {
	return objectCode;
    }

    @Observable
    public AbstractAvlMachine<T>  setEnabled(final boolean enabled) {
	this.enabled = enabled;
	return this;
    }

    public boolean isEnabled() {
	return enabled;
    }

    @Observable
    public AbstractAvlMachine<T>  setDeletedTime(final Date deletedTime) {
	this.deletedTime = deletedTime;
	return this;
    }

    public Date getDeletedTime() {
	return deletedTime;
    }


    @Observable
    public AbstractAvlMachine<T>  setDeleted(final boolean deleted) {
	this.deleted = deleted;
	return this;
    }

    public boolean isDeleted() {
	return deleted;
    }
}