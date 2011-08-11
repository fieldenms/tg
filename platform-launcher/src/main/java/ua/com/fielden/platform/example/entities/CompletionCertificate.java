/**
 *
 */
package ua.com.fielden.platform.example.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * Class representing completion certificate entity with list of rotables (and
 * additional parameters) to be estimated, repairing workshop which should be
 * always contractor workshop, raised date and status (now it is substituted by
 * atContractorWorkshop).
 * 
 * @author Yura
 */
@KeyType(Long.class)
public class CompletionCertificate extends AbstractEntity<Long> {

    private static final long serialVersionUID = 1L;

    @IsProperty(CompletionCertificateEntry.class)
    private final Set<CompletionCertificateEntry> entries = new HashSet<CompletionCertificateEntry>();

    @IsProperty
    private Date dateRaised;
    @IsProperty
    private Workshop initiatedAt;
    @IsProperty
    private Person initiatedBy;
    @IsProperty
    private Person acceptedBy;
    @IsProperty
    private Date dateAccepted;
    @IsProperty
    private CompletionCertificateStatus status = CompletionCertificateStatus.E;

    protected CompletionCertificate() {
    }

    public CompletionCertificate(final Long key, final String desc, final Person contractorUser, final Workshop initiatedAt) {
	super(null, key, desc);
	setDateRaised(new Date());
	setInitiatedBy(contractorUser);
	setInitiatedAt(initiatedAt);
    }

    public List<CompletionCertificateEntry> getEntries() {
	return new ArrayList<CompletionCertificateEntry>(entries);
    }

    @Override
    protected void setId(final Long id) {
	super.setId(id);
	setKey(id);
    }

    /**
     * Adds completion certificate entry to the list of entries.
     * 
     * @param entry
     * @return
     */
    public Result addEntry(final CompletionCertificateEntry entry) {
	if (!this.equals(entry.getCompletionCertificate())) {
	    return new Result(entry, new Exception("This entry is already associated with a different completion certificate."));
	}
	if (!entries.contains(entry)) {
	    entries.add(entry);
	    return new Result(entry, "Added entry successfully.");
	}
	return new Result(entry, new Exception("An attempt to add duplicate entry."));
    }

    public Workshop getInitiatedAt() {
	return initiatedAt;
    }

    /**
     * Looks for a entry with the given rotable. There can be one or none of
     * such entries.
     * 
     * @param rotable
     * @return
     */
    public CompletionCertificateEntry getEntry(final Rotable rotable) {
	for (final CompletionCertificateEntry entry : entries) {
	    if (entry.getRotable().equals(rotable)) {
		return entry;
	    }
	}
	return null;
    }

    @NotNull
    @Final
    @EntityExists(Workshop.class)
    @Observable
    protected CompletionCertificate setInitiatedAt(final Workshop repairingWorkshop) {
	if (!repairingWorkshop.isContractorWorkshop()) {
	    throw new IllegalArgumentException("initiatedAt work should be contractor workshop");
	}
	this.initiatedAt = repairingWorkshop;
	return this;
    }

    public Date getDateRaised() {
	return dateRaised;
    }

    @NotNull
    @Final
    @Observable
    public CompletionCertificate setDateRaised(final Date raisedDate) {
	this.dateRaised = raisedDate;
	return this;
    }

    public Person getInitiatedBy() {
	return initiatedBy;
    }

    @NotNull
    @Final
    @EntityExists(Person.class)
    @Observable
    protected void setInitiatedBy(final Person contractorUser) {
	this.initiatedBy = contractorUser;
    }

    public Person getAcceptedBy() {
	return acceptedBy;
    }

    @Final
    @EntityExists(Person.class)
    @Observable
    public CompletionCertificate setAcceptedBy(final Person acceptedBy) {
	this.acceptedBy = acceptedBy;
	return this;
    }

    public CompletionCertificateStatus getStatus() {
	return status;
    }

    @NotNull
    @Observable
    public CompletionCertificate setStatus(final CompletionCertificateStatus status) {
	this.status = status;
	return this;
    }

    public Money getTotalPrice() {
	Money totalPrice = new Money(new BigDecimal(0));
	for (final CompletionCertificateEntry entry : entries) {
	    if (entry.getPrice() != null) {
		totalPrice = totalPrice.plus(entry.getPrice());
	    }
	}
	return totalPrice;
    }

    /**
     * Checks also whether repairing workshop is contractor workshop and whether
     * all {@link CompletionCertificateEntry}s were estimated (i.e.
     * estimatedPrice != null)
     */
    @Override
    public Result validate() {
	final Result superResult = super.validate();
	if (!superResult.isSuccessful()) {
	    return superResult;
	}

	if (!getInitiatedAt().isContractorWorkshop()) {
	    return new Result(this, new IllegalArgumentException("Initiated at workshop is a required entry."));
	}

	for (final CompletionCertificateEntry completionCertificateEntry : getEntries()) {
	    final Result result = completionCertificateEntry.isValid();
	    if (!result.isSuccessful()) {
		return result;
	    }
	}

	return new Result(this, "CC " + this + " is valid.");
    }

    /**
     * Convenience method for checking of completion certificate is active.
     * 
     * @return
     */
    public boolean isActive() {
	return CompletionCertificateStatus.getActiveStatuses().contains(getStatus());
    }

    public Date getDateAccepted() {
	return dateAccepted;
    }

    @NotNull
    @Final
    @Observable
    public CompletionCertificate setDateAccepted(final Date dateAccepted) {
	this.dateAccepted = dateAccepted;
	return this;
    }

    /**
     * Iterates though entries and check if there are incomplete ones.
     * 
     * @return -- false if there are incomplete entries, true -- otherwise.
     */
    public boolean allEntriesCompleted() {
	for (final CompletionCertificateEntry entry : entries) {
	    if (!entry.isCompleted()) {
		return false;
	    }
	}
	return true;
    }
}
