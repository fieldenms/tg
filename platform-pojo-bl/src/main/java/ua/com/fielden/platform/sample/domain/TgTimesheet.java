package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;

@KeyTitle("Timesheet")
@DescTitle("Description")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@DefaultController(ITgTimesheet.class)
public class TgTimesheet  extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Person", desc = "TgTimesheet  owner")
    @CompositeKeyMember(1)
    private String person;

    @IsProperty
    @MapTo
    @Title(value = "Start Date", desc = "Date when the TgTimesheet  entry was made")
    @Dependent("finishDate")
    @CompositeKeyMember(2)
    private Date startDate;

    @IsProperty
    @MapTo
    @Title(value = "Finish Date", desc = "Date when the TgTimesheet entry was completed")
    @Dependent("startDate")
    private Date finishDate;

    @IsProperty
    @MapTo
    @Readonly
    @Title(value = "Duration", desc = "TgTimesheet  duration (hours)")
    private BigDecimal duration;

    @IsProperty
    @MapTo
    @Title(value = "Incident", desc = "Incident for which this quotation is created")
    private String incident;

    @Observable
    public TgTimesheet  setDuration(final BigDecimal duration) {
	this.duration = duration;
	return this;
    }

    public BigDecimal getDuration() {
	return duration;
    }

    @Observable
    @GeProperty("startDate")
    public TgTimesheet  setFinishDate(final Date finishDate) {
	this.finishDate = finishDate;
	return this;
    }

    public Date getFinishDate() {
	return finishDate;
    }

    @Observable
    @LeProperty("finishDate")
    public TgTimesheet  setStartDate(final Date startDate) {
	this.startDate = startDate;
	return this;
    }

    public Date getStartDate() {
	return startDate;
    }

    @Observable
    public TgTimesheet  setPerson(final String person) {
	this.person = person;
	return this;
    }

    public String getPerson() {
	return person;
    }

    public String getIncident() {
        return incident;
    }

    @Observable
    public TgTimesheet setIncident(final String incident) {
        this.incident = incident;
        return this;
    }

}
