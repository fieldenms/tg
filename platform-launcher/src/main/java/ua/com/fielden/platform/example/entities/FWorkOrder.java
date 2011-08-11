package ua.com.fielden.platform.example.entities;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "Wo No", desc = "Work order number")
@DescTitle(value = "Description", desc = "Work order description")
public class FWorkOrder extends AbstractEntity<String> {

    private static final long serialVersionUID = 2347594331214760853L;

    @IsProperty
    @Title(value = "Workshop No", desc = "Workshop where workorder was created")
    private Workshop workshop;

    @IsProperty
    @Title(value = "Person", desc = "Person who has created this work order")
    private Person person;

    @IsProperty
    @Title(value = "Create date")
    private Date createDate;

    @IsProperty
    @Title(value = "Workorder entity", desc = "Entity for which workorder was created")
    private Workorderable workorderable;

    public Workshop getWorkshop() {
	return workshop;
    }

    @Observable
    public void setWorkshop(final Workshop workshop) {
	this.workshop = workshop;
    }

    public Person getPerson() {
	return person;
    }

    @Observable
    public void setPerson(final Person person) {
	this.person = person;
    }

    public Date getCreateDate() {
	return createDate;
    }

    @Observable
    public void setCreateDate(final Date createDate) {
	this.createDate = createDate;
    }

    public Workorderable getWorkorderable() {
	return workorderable;
    }

    @Observable
    public void setWorkorderable(final Workorderable workorderable) {
	this.workorderable = workorderable;
    }

}
