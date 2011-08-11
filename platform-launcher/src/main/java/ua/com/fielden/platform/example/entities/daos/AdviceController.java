package ua.com.fielden.platform.example.entities.daos;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.ValidationException;

import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.example.entities.Advice;
import ua.com.fielden.platform.example.entities.AdvicePosition;
import ua.com.fielden.platform.example.entities.IAdviceController;
import ua.com.fielden.platform.example.entities.IAdviceDao;
import ua.com.fielden.platform.example.entities.ICompletionCertificateController;
import ua.com.fielden.platform.example.entities.IRotableDao;
import ua.com.fielden.platform.example.entities.Person;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Workshop;

import com.google.inject.Inject;

/**
 * DAO based implementation of the business logic defined by {@link IAdviceController}.
 *
 * @author 01es
 *
 */
public class AdviceController implements IAdviceController {
    private final IAdviceDao adviceDao;
    private final IRotableDao rotableDao;
    private final ICompletionCertificateController ccController;

    @Inject
    public AdviceController(final IAdviceDao adviceDao, final IRotableDao rotableDao, final ICompletionCertificateController ccController) {
	this.adviceDao = adviceDao;
	this.rotableDao = rotableDao;
	this.ccController = ccController;
    }

    /**
     * Retrieves an advice by key.
     *
     * @param key
     * @return
     */
    @Override
    public Advice get(final Long key) {
	return getAdviceDao().findByKey(key);
    }

    /**
     * Saves advice using provided DAO.
     *
     * @param advice
     */
    public void save(final Advice advice) {
	adviceDao.save(advice);
    }

    /**
     * Implements the logic of placing rotables onto an advice. Rotables can be added if advice is provided (e.i. not null), not received and has enough vacant positions.
     * <p>
     * If rotables have been added successfully then advice is saved.
     *
     * @param advice --
     *                target {@link Advice}
     * @param rotables --
     *                a list of {@link Rotable} instances to be placed on advice
     * @param sendingWorkshop --
     *                workshop used as sending for newly placed rotables
     * @param receivingWorkshop --
     *                workshop used as receiving for newly placed rotables
     * @param person --
     *                used to indicate who placed rotables onto advice
     */
    @Transactional
    public Map<Rotable, Result> addRotablesToAdvice(final Advice advice, final List<Rotable> rotables, final Workshop sendingWorkshop, final Workshop receivingWorkshop, final Person person)
	    throws Result {
	// first let's do some validation
	if (advice == null) {
	    throw new Result(null, new Exception("No advice is selected. Please select an advice and try again."));
	}
	if (advice.isReceived()) {
	    throw new Result(advice, new Exception("Advice cannot be modified after it has been fully received."));
	}
	if (sendingWorkshop == null) {
	    throw new Result(advice, new Exception("Sending workshop is required."));
	}
	if (receivingWorkshop == null) {
	    throw new Result(advice, new Exception("Receiving workshop is required."));
	}
	if (person == null) {
	    throw new Result(advice, new Exception("Responsible person is required."));
	}
	final List<AdvicePosition> vacantPositions = advice.getVacantPositions();
	if (vacantPositions.size() < rotables.size()) {
	    throw new Result(advice, new Exception("Not enough positions (" + advice.getNumberOfVacantPositions() + ") to fit " + rotables.size() + " rotables.\n"
		    + "Please reduce the number of selected rotables and try again."));
	}
	// if we are here then we can process rotables
	final Map<Rotable, Result> results = new HashMap<Rotable, Result>();

	for (int index = 0; index < rotables.size(); index++) {
	    final Rotable rotable = rotables.get(index);
	    try {
		final Result checkResult = ccController.check(rotable, true, sendingWorkshop);
		if (checkResult.isSuccessful()) {
		    // TODO record movement history
		    vacantPositions.get(index).setRotable(rotable).setPlacedBy(person).setPlacementDate(new Date()).setReceivingWorkshop(receivingWorkshop).setSendingWorkshop(sendingWorkshop);
		    rotable.setLocation(vacantPositions.get(index));
		    rotableDao.save(rotable);
		} else {
		    results.put(rotable, checkResult);
		}
	    } catch (final Result e) {
		e.printStackTrace();
		results.put(rotable, e);
	    }
	}
	save(advice);
	return results;
    }

    /**
     * Validates whether advice can be dispatched to the provided workshop. Marks advice as dispatched by setting the {@link Advice#setDispatchedToWorkshop(Workshop)} and updates
     * the dispatch date. Saves changes.
     *
     * TODO unit test
     *
     * @param advice
     * @param workshop
     * @throws ValidationException
     */
    @Transactional
    public void dispatch(final Advice advice, final Workshop workshop) throws Result {
	// validate workshop
	final Result result = workshop.isContractorWorkshop() ? canBeDispatchedToContractor(advice) : canBeDispatchedToPnl(advice);
	if (!result.isSuccessful()) {
	    throw result;
	}
	final Result validationResult = advice.isValid();
	if (!validationResult.isSuccessful()) {
	    throw validationResult ;
	}
	advice.setDispatchedToWorkshop(workshop);
	advice.setDateDispatched(new Date());
	save(advice);
    }

    /**
     * Receives a rotable in the specified position into the workshop, but only if that workshop matches the receiving workshop.
     * <p>
     * At first, checks whether passed {@link AdvicePosition} is valid. If not throws validation {@link Result} as exception
     * <p>
     * As the result relevant rotable movement records are created and advice position is updated.
     * <P>
     * If position could not be processed a relevant error is returned.
     * <p>
     * At the end of the method execution the advice is checked for remaining pending positions. If there are no such positions, advice is marked as received, which prevents its
     * further usage.
     * <p>
     * All changes are saved.
     *
     * TODO unit test
     *
     * @param positions
     * @param workshop
     */
    @Transactional
    public Result receive(final Advice advice, final AdvicePosition position, final Workshop workshop) throws Result {
	final Result validationResult = position.isValid();
	if(!validationResult.isSuccessful()) {
	    throw validationResult;
	}
	if (position.isPending()) {
	    // TODO record movement history
	    final Rotable rotable = position.getRotable();
	    if (workshop.equals(position.getSendingWorkshop()) && workshop.equals(position.getReceivingWorkshop())) {
		final Result checkResult = ccController.check(position.getRotable(), false, workshop);
		if (!checkResult.isSuccessful()) {
		    return checkResult;
		}
		rotable.setLocation(workshop);
		position.clear();
	    } else if (workshop.equals(position.getReceivingWorkshop())) {
		rotable.setLocation(workshop);
		position.setReceived(true).setReceivedDate(new Date());
	    } else {
		return new Result(position, new Exception("Receiving workshop " + position.getReceivingWorkshop().getKey() + " is different to the current " + workshop.getKey()));
	    }
	    rotableDao.save(rotable);
	} else {
	    return new Result(position, new Exception("Advice position #" + position.getPosition() + " is not pending."));
	}
	if (!advice.hasPendingPositions()) {
	    advice.setReceived(true);
	}
	save(advice);
	return new Result(position, "Received.");
    }

    /**
     * An advice cannot be dispatched to contractor workshop only if it was initiated at contractor workshop and is already dispatched to a PNL workshop.
     *
     * @return
     */
    public Result canBeDispatchedToContractor(final Advice advice) {
	if (advice.getInitiatedAtWorkshop() == null) {
	    return new Result(advice, new IllegalStateException("Cannot dispatch advice with no initiated workshop."));
	}
	final boolean cannot = advice.getInitiatedAtWorkshop().isContractorWorkshop() && advice.getDispatchedToWorkshop() != null
		&& !advice.getDispatchedToWorkshop().isContractorWorkshop();

	return cannot ? new Result(advice, new IllegalStateException("Cannot dispatch to a contractor workshop -- already dispatched to a PNL workshop."))
		: new Result(advice, "Can dispatch to a contractor workshop.");
    }

    /**
     * An advice cannot be dispatched to PNL workshop only if it was initiated at PNL workshop and is already dispatched to a contractor workshop.
     *
     * @return
     */
    public Result canBeDispatchedToPnl(final Advice advice) {
	if (advice.getInitiatedAtWorkshop() == null) {
	    return new Result(advice, new IllegalStateException("Cannot dispatch advice with no initiated workshop."));
	}

	final boolean cannot = !advice.getInitiatedAtWorkshop().isContractorWorkshop() && advice.getDispatchedToWorkshop() != null
		&& advice.getDispatchedToWorkshop().isContractorWorkshop();

	return cannot ? new Result(advice, new IllegalStateException("Cannot dispatch to a PNL workshop -- already dispatched to a contractor workshop."))
		: new Result(advice, "Can dispatch to a PNL workshop.");
    }

    public IAdviceDao getAdviceDao() {
	return adviceDao;
    }

    @Override
    public List<Advice> findActive(final Workshop workshop) {
	return adviceDao.findActiveFor(workshop);
    }
}
