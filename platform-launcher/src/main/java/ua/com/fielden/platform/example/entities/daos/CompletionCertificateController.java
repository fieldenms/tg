package ua.com.fielden.platform.example.entities.daos;

import java.util.Date;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.example.entities.CompletionCertificate;
import ua.com.fielden.platform.example.entities.CompletionCertificateEntry;
import ua.com.fielden.platform.example.entities.CompletionCertificateStatus;
import ua.com.fielden.platform.example.entities.IAdviceController;
import ua.com.fielden.platform.example.entities.ICompletionCertificateController;
import ua.com.fielden.platform.example.entities.ICompletionCertificateDao;
import ua.com.fielden.platform.example.entities.Person;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Workshop;

import com.google.inject.Inject;

/**
 * This is a Hibernate-based implementation of {@link ICompletionCertificateController}.
 *
 * @author 01es
 *
 */
public class CompletionCertificateController implements ICompletionCertificateController {
    private final ICompletionCertificateDao dao;

    @Inject
    public CompletionCertificateController(final ICompletionCertificateDao dao) {
	this.dao = dao;
    }

    @Override
    public Result accept(final CompletionCertificate cc, final Person person) {
	try {
	    cc.setAcceptedBy(person).setDateAccepted(new Date()).setStatus(CompletionCertificateStatus.A);
	    save(cc);
	    return new Result(cc, "Accepted.");
	} catch (final RuntimeException e) {
	    e.printStackTrace();
	    return new Result(cc, "Acceptance failed.", new Exception("Acceptance failed."));
	}
    }

    @Override
    public Result add(final CompletionCertificate cc, final Rotable rotable) {
	// perform some validation
	if (cc == null) {
	    return new Result(null, new IllegalArgumentException("Please provide a completion certificate."));
	}
	if (rotable == null) {
	    return new Result(null, new IllegalArgumentException("Please provide a rotable for placing onto completion certificate."));
	}
	if (CompletionCertificateStatus.E != cc.getStatus()) {
	    return new Result(cc, new IllegalStateException("Rotables can be placed onto completion certificate only when it has status " + CompletionCertificateStatus.E + "."));
	}
	final CompletionCertificate prevCc = getDao().findLatest(rotable);
	if (prevCc != null && prevCc.isActive()) {
	    return new Result(rotable,
		    new IllegalStateException("Rotable " + rotable.getKey() + " is already associated with an active completion certificate #" + prevCc.getKey()));
	}
	// try to add new entry
	final Result result = cc.addEntry(new CompletionCertificateEntry(cc, rotable, new Date()));
	save(cc);
	return result;
    }

    /**
     * Looks for a completion certificate associated with the specified rotable and performs one of the following actions:
     * <ul>
     * <li>If parameter addedToAdvice is true indicating placement of the rotable onto an advice and there is an active CC then update the corresponding CC entry to be completed
     * and check if CC should also be completed.</li>
     * <li>If parameter addedToAdvice is true indicating placement of the rotable onto an advice and there is an inactive CC (uses the latest one) or there is no CC at all then the
     * returned result should indicate an error and placement of the rotable onto advice should be aborted.</li>
     *
     * <li>If parameter addedToAdvice is false indicating advice position correction and there is an active CC then update the corresponding CC entry to be incomplete.</li>
     * <li>If parameter addedToAdvice is false indicating advice position correction and there is an inactive CC (uses the latest one) then make both corresponding CC entry and CC
     * incomplete.</li>
     * <li>If parameter addedToAdvice is false indicating advice position correction and there is no CC at all then the returned result should indicate an error and placement of
     * the rotable onto advice should be aborted. However, this should never happen due to the fact that placement of rotables onto advices is validated against a completion
     * certificate.</li>
     * </ul>
     * <p>
     * This method should never be a direct user action. And the only intention is for it to be used as part of {@link IAdviceController}.
     *
     * @param rotable
     *            -- rotable to be placed/removed onto/from an advice
     * @param addedToAdvice
     *            -- a flag indicating whether rotable is being placed/removed onto/from an advice
     * @param workshop
     *            -- operating workshop, i.e. workshop that is used as the current one
     * @return
     */
    @Override
    public Result check(final Rotable rotable, final boolean addedToAdvice, final Workshop workshop) {
	if (!workshop.isContractorWorkshop()) {
	    return new Result(rotable, "Current workshop belongs to PNL, which means that there is no need to validate completion certificates.");
	}
	// workshop belongs to contractor, and thus there is a need to ensure existence of completion certificate for a given rotable
	// and perform necessary validation/manipulation
	final CompletionCertificate cc = getDao().findLatest(rotable);

	if (addedToAdvice) { // rotable is being added onto an advice
	    if (cc == null || !cc.isActive()) {
		return new Result(rotable, new Exception("Rotable " + rotable.getKey()
			+ " is not associated with an active completion certificate, which is required for placement of rotable onto an advice."));
	    }
	    // completion certificate is active let's update it and the corresponding to the rotable entry
	    final CompletionCertificateEntry entry = cc.getEntry(rotable);
	    entry.setCompleted(true);
	    if (cc.allEntriesCompleted()) {
		cc.setStatus(cc.getStatus().next());
	    }
	    save(cc);
	    return new Result(cc, "Completion certificate has been updated successfully.");
	}
	// rotable is being moved from an advice as the result of corrective action -- should not be mixed with receiving!
	if (cc == null) {
	    return new Result(rotable, new Exception("Rotable " + rotable.getKey()
		    + " is not associated with any completion certificate. This indicates data inconsistency. Please contact application support for more details."));
	}
	// rotable is being returned, therefore need to make corresponding to the rotable entry active again.
	cc.getEntry(rotable).setCompleted(false);
	if (!cc.isActive()) { // cc is inactive, so make active too
	    cc.setStatus(cc.getStatus().next());
	}
	save(cc);
	return new Result(cc, "Completion certificate has been updated successfully.");
    }

    @Override
    public Result makeReady(final CompletionCertificate cc, final Person person) {
	if (!cc.getInitiatedBy().equals(person)) {
	    return new Result(cc, new IllegalArgumentException("Only originator of the completion certificate can mark it ready for review by PNL."));
	}
	cc.setStatus(CompletionCertificateStatus.R);
	save(cc);
	return new Result(cc, "Completion certificate is ready for review by PNL.");
    }

    @Override
    public void save(final CompletionCertificate cc) {
	getDao().save(cc);
    }

    @Override
    public CompletionCertificate get(final Long key) {
	return getDao().findByKey(key);
    }

    public ICompletionCertificateDao getDao() {
	return dao;
    }
}
