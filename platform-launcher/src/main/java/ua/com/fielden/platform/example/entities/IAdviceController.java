package ua.com.fielden.platform.example.entities;

import java.util.List;
import java.util.Map;

import javax.xml.bind.ValidationException;

import ua.com.fielden.platform.error.Result;

/**
 * Defines business logic contract relevant for advice and its positions.
 *
 * @author 01es
 *
 */
public interface IAdviceController {
    /**
     * Retrieves an advice by key.
     *
     * @param key
     * @return
     */
    public Advice get(final Long key);

    public Map<Rotable, Result> addRotablesToAdvice(final Advice advice, final List<Rotable> rotables, final Workshop workshop, final Workshop receivingWorkshop, final Person person) throws Result;

    /**
     * Saves advice using provided DAO.
     *
     * @param advice
     */
    public void save(final Advice advice);

    /**
     * Marks advice as dispatched by setting the {@link Advice#setDispatchedToWorkshop(Workshop)} and updates the dispatch date.
     *
     * <p>
     * Saves changes.
     *
     * @param advice
     * @param workshop
     * @throws ValidationException
     */
    public void dispatch(final Advice advice, final Workshop workshop) throws Result;

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
     * @param positions
     * @param workshop
     */
    public Result receive(final Advice advice, final AdvicePosition position, final Workshop workshop) throws Result;

    /**
     * Validates whether advice can be dispatched to a contractor workshop
     *
     * @return
     */
    public Result canBeDispatchedToContractor(final Advice advice);

    /**
     * Validates whether advice can be dispatched to a PNL workshop
     *
     * @return
     */
    public Result canBeDispatchedToPnl(final Advice advice);

    /**
     * Should return active for workshop advices.
     *
     * @param workshop
     * @return
     */
    public List<Advice> findActive(final Workshop workshop);
}
