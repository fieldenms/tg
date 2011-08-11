/**
 *
 */
package ua.com.fielden.platform.example.entities.daos;

import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.CompletionCertificate;
import ua.com.fielden.platform.example.entities.CompletionCertificateEntry;
import ua.com.fielden.platform.example.entities.CompletionCertificateStatus;
import ua.com.fielden.platform.example.entities.ICompletionCertificateDao;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Workshop;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO to manage {@link CompletionCertificate}, which implements {@link ICompletionCertificateDao} to support additional to standard DAO logic.
 *
 * @author TG Team
 */
@EntityType(CompletionCertificate.class)
public class CompletionCertificateDao extends CommonEntityDao<CompletionCertificate> implements ICompletionCertificateDao {

    @Inject
    protected CompletionCertificateDao(final IFilter filter) {
	super(filter);
    }

    /**
     * Should return all CompletionCertificate instances in status E, R, A.
     */
    @SuppressWarnings("unchecked")
    @Override
    @SessionRequired
    public List<CompletionCertificate> findActive() {
	return getSession().createQuery("from " + CompletionCertificate.class.getName() + " as cc " +
			"where cc.status in (:in_active_statuses) order by cc.key")
			.setParameterList("in_active_statuses", CompletionCertificateStatus.getActiveStatuses())
			.list();
    }

    /**
     * Should return all CompletionCertificate in status E, R, A initiated at the provided workshop.
     */
    @SuppressWarnings("unchecked")
    @Override
    @SessionRequired
    public List<CompletionCertificate> findActive(final Workshop workshop) {
	return getSession().createQuery("from " + CompletionCertificate.class.getName() + " as cc " +
			"where cc.status in (:in_active_statuses) " +
			"and cc.initiatedAt.id = :in_workshop_id " +
			"order by cc.key")
			.setParameterList("in_active_statuses", CompletionCertificateStatus.getActiveStatuses())
			.setParameter("in_workshop_id", workshop.getId())
			.list();
    }

    /**
     * Looks for the latest CompletionCertificate containing entry with the specified rotable.
     *
     * @param rotable
     * @return Instance of an active or inactive CompletionCertificate if rotable is associated with one, or null otherwise
     */
    @Override
    @SessionRequired
    public CompletionCertificate findLatest(final Rotable rotable) {
	return (CompletionCertificate) getSession().createQuery("from " + CompletionCertificate.class.getName() + " as cc " +
		"where exists (from " + CompletionCertificateEntry.class.getName() + " as cce where cce.completionCertificate = cc and cce.rotable.id = :in_rotable_id) " +
		"order by cc.dateRaised desc")
		.setParameter("in_rotable_id", rotable.getId())
		.setMaxResults(1)
		.uniqueResult();
    }
}
