package ua.com.fielden.platform.example.entities;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Determines contract specific for obtaining CompletionCertificate instances.
 *
 * @author 01es
 *
 */
public interface ICompletionCertificateDao extends IEntityDao<CompletionCertificate> {
    /**
     * Should return all CC in status E, R, A.
     */
    List<CompletionCertificate> findActive();

    /**
     * Should return all CC in status E, R, A initiated at the provided workshop.
     */
    List<CompletionCertificate> findActive(final Workshop workshop);

    /**
     * Looks for the latest CompletionCertificate containing entry with the specified rotable.
     *
     * @param rotable
     * @return Instance of an active or inactive CompletionCertificate if rotable is associated with one, or null otherwise
     */
    CompletionCertificate findLatest(final Rotable rotable);
}
