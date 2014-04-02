package ua.com.fielden.platform.example.entities;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle(value = "Equipment", desc = "Equipment entity")
@KeyTitle("Equipment No")
@DescTitle("Description")
@KeyType(String.class)
public class FEquipment extends AbstractEntity<String> {

    private static final long serialVersionUID = -525280318617226577L;

    @IsProperty
    @Title("Common property")
    private String commonProperty;
    @IsProperty
    @Title(value = "Eqp. Class", desc = "Equipment class")
    private EqClass eqClass;
    @IsProperty
    @Title(value = "Init. date", desc = "The date when this equipment was initiated")
    private Date initDate;
    @IsProperty
    @Title(value = "Serial No.", desc = "Serial number")
    private String number;

    public String getCommonProperty() {
        return commonProperty;
    }

    @Observable
    public void setCommonProperty(final String commonProperty) {
        this.commonProperty = commonProperty;
    }

    public EqClass getEqClass() {
        return eqClass;
    }

    @Observable
    public void setEqClass(final EqClass eqClass) {
        this.eqClass = eqClass;
    }

    public Date getInitDate() {
        return initDate;
    }

    @Observable
    public void setInitDate(final Date initDate) {
        this.initDate = initDate;
    }

    public String getNumber() {
        return number;
    }

    @Observable
    public void setNumber(final String number) {
        this.number = number;
    }

}
