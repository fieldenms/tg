package ua.com.fielden.platform.gis.gps;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 * 
 * @author Developers
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "IMEI", desc = "Унікальний номер IMEI для ідентифікації пристрою в GSM та інших мережах")
@EntityTitle(value = "Модуль", desc = "Мобільний термінал із GPS та GSM зв'язком")
// TODO do not forget to provide companion object in its descendants -- @CompanionObject(IModule.class)
@MapEntityTo
@DescTitle(value = "Нотатки", desc = "Нотатки про цей модуль")
public abstract class AbstractAvlModule extends AbstractEntity<String> {
    private static final long serialVersionUID = -629369785111957096L;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Серійний Номер", desc = "Серійний Номер")
    private Integer serialNo;

    //    @IsProperty
    //    @Required
    //    @MapTo
    //    @Title(value = "Type", desc = "Module type")
    //    private ModuleType moduleType;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "GPS Прошивка", desc = "Прошивка GPS приймача")
    private String gpsFirmware;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "HW версія", desc = "Версія апаратного (hardware) забезпечення")
    private String hwVersion;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Imlet версія", desc = "Іmlet версія")
    private String imletVersion;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Ідентифікатор", desc = "Ідентифікатор для конфігураційних команд та запитів")
    private String identifier;

    @IsProperty
    @MapTo
    @Title(value = "Пароль", desc = "Пароль для конфігураційних команд та запитів")
    private String password;

    @Observable
    public AbstractAvlModule setPassword(final String password) {
        this.password = password;
        return this;
    }

    public String getPassword() {
        return password;
    }

    @Observable
    public AbstractAvlModule setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Observable
    public AbstractAvlModule setImletVersion(final String imletVersion) {
        this.imletVersion = imletVersion;
        return this;
    }

    public String getImletVersion() {
        return imletVersion;
    }

    @Observable
    public AbstractAvlModule setHwVersion(final String hwVersion) {
        this.hwVersion = hwVersion;
        return this;
    }

    public String getHwVersion() {
        return hwVersion;
    }

    @Observable
    public AbstractAvlModule setGpsFirmware(final String gpsFirmware) {
        this.gpsFirmware = gpsFirmware;
        return this;
    }

    public String getGpsFirmware() {
        return gpsFirmware;
    }

    @Observable
    public AbstractAvlModule setSerialNo(final Integer serialNo) {
        this.serialNo = serialNo;
        return this;
    }

    public Integer getSerialNo() {
        return serialNo;
    }

    //    @Observable
    //    public Module setModuleType(final ModuleType type) {
    //	this.moduleType = type;
    //	return this;
    //    }
    //
    //    public ModuleType getModuleType() {
    //	return moduleType;
    //    }

}