/**
 *
 */
package ua.com.fielden.platform.example.entities;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * This class represents {@link Rotable} from {@link Advice} but from the
 * contractors point of view, i.e. {@link Rotable} itself,
 * {@link CompletionCertificate} to which it belongs,
 * 
 * @author Yura
 */
@KeyType(DynamicEntityKey.class)
public class CompletionCertificateEntry extends AbstractEntity<DynamicEntityKey> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    private CompletionCertificate completionCertificate;

    @IsProperty
    @CompositeKeyMember(2)
    private Rotable rotable;

    @IsProperty
    private Date date;

    @IsProperty
    private Money price;

    /**
     * Completion Certificate entry is considered completed if the corresponding
     * rotable was placed onto an advice.
     */
    @IsProperty
    private boolean completed;

    protected CompletionCertificateEntry() {
	super(null, null, "");
	setKey(new DynamicEntityKey(this));
    }

    public CompletionCertificateEntry(final CompletionCertificate completionCertificate, final Rotable rotable, final Date date) {
	this();
	this.completionCertificate = completionCertificate;
	this.rotable = rotable;
	this.date = date;
	this.completed = false;
    }

    public Money getPrice() {
	return price;
    }

    @NotNull
    @Observable
    public CompletionCertificateEntry setPrice(final Money estimatedPrice) {
	this.price = estimatedPrice;
	return this;
    }

    public CompletionCertificate getCompletionCertificate() {
	return completionCertificate;
    }

    public Rotable getRotable() {
	return rotable;
    }

    public Date getDate() {
	return date;
    }

    @NotNull
    @Final
    @EntityExists(CompletionCertificate.class)
    @Observable
    protected CompletionCertificateEntry setCompletionCertificate(final CompletionCertificate completionCertificate) {
	this.completionCertificate = completionCertificate;
	return this;
    }

    @NotNull
    @Final
    @EntityExists(Rotable.class)
    @Observable
    protected CompletionCertificateEntry setRotable(final Rotable rotable) {
	this.rotable = rotable;
	return this;
    }

    @NotNull
    @Final
    @Observable
    public CompletionCertificateEntry setDate(final Date placementDate) {
	this.date = placementDate;
	return this;
    }

    /**
     * Checks also whether {@link #price} is not null, i.e. whether contractor
     * has estimated repair costs
     */
    @Override
    public Result validate() {
	final Result superResult = super.validate();
	if (!superResult.isSuccessful()) {
	    return superResult;
	}

	if (getPrice() == null) {
	    return new Result(this, new IllegalArgumentException("Price is required."));
	}

	return new Result(this, "CC entry " + this + " is valid.");
    }

    public boolean isCompleted() {
	return completed;
    }

    @Observable
    public void setCompleted(final boolean completed) {
	this.completed = completed;
    }

}
