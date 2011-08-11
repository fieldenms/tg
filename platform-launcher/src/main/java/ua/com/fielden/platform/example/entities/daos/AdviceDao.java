package ua.com.fielden.platform.example.entities.daos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.Advice;
import ua.com.fielden.platform.example.entities.AdvicePosition;
import ua.com.fielden.platform.example.entities.IAdviceDao;
import ua.com.fielden.platform.example.entities.Workshop;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link Advice} retrieval.
 *
 * @author TG Team
 */
@EntityType(Advice.class)
public class AdviceDao extends CommonEntityDao<Advice> implements IAdviceDao {

    @Inject
    protected AdviceDao(final IFilter filter) {
	super(filter);
    }

    /**
     * Retrieves advices active for the specified workshop using the definition of "active" as determined in {@link Advice#isActiveForContractor()} and {@link Advice#isActiveForPnl()}.
     *
     * @param workshop
     * @return
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    public List<Advice> findActiveFor(final Workshop workshop) {
	final List<Advice> advices = new ArrayList<Advice>();
	if (!workshop.isContractorWorkshop()) { // is PNL?
	    final String workshopFilteringCondition =
		"(a.initiatedAtWorkshop.id = :in_workshop_id or (a.dispatchedToWorkshop is not null and a.dispatchedToWorkshop.id = :in_workshop_id) or " +
		"exists (from " + AdvicePosition.class.getName() + " as p where p.advice = a and p.rotable is not null and p.received <> true and (p.sendingWorkshop.id = :in_workshop_id or p.receivingWorkshop.id = :in_workshop_id))) ";
	    // there are two queries to correctly handle situations where dispatchedToWorkshop is null and not null
	    final Query query1 = getSession().createQuery("from " + Advice.class.getName() + " as a where a.received <> true and a.dispatchedToWorkshop is null and "
		    + "a.initiatedAtWorkshop.contractorWorkshop <> true and " + workshopFilteringCondition + " order by a.key");
	    advices.addAll(query1.setLong("in_workshop_id", workshop.getId()).list());
	    final Query query2 = getSession().createQuery("from " + Advice.class.getName() + " as a where a.received <> true "
		    + "and a.dispatchedToWorkshop is not null and a.dispatchedToWorkshop.contractorWorkshop <> true "
		    + "and " + workshopFilteringCondition + "order by a.key");
	    advices.addAll(query2.setLong("in_workshop_id", workshop.getId()).list());
	} else { // is contractor workshop
	    final String workshopFilteringCondition =
		"(a.initiatedAtWorkshop.id = :in_workshop_id or (a.dispatchedToWorkshop is not null and a.dispatchedToWorkshop.id = :in_workshop_id) or " +
		" exists (from " + AdvicePosition.class.getName() + " as p where p.advice = a and p.rotable is not null and p.received <> true and (p.sendingWorkshop.id = :in_workshop_id or p.receivingWorkshop.id = :in_workshop_id)))";
	    final Query query1 = getSession().createQuery("from " + Advice.class.getName() + " as a where a.received <> true and a.dispatchedToWorkshop is null and "
		    + "a.initiatedAtWorkshop.contractorWorkshop = true and " + workshopFilteringCondition + " order by a.key");
	    advices.addAll(query1.setLong("in_workshop_id", workshop.getId()).list());
	    final Query query2 = getSession().createQuery("from " + Advice.class.getName() + " as a where a.received <> true "
		    + " and a.dispatchedToWorkshop is not null and a.dispatchedToWorkshop.contractorWorkshop = true"
		    + " and " + workshopFilteringCondition + " order by a.key");
	    advices.addAll(query2.setLong("in_workshop_id", workshop.getId()).list());
	}
	return advices;
    }
}
