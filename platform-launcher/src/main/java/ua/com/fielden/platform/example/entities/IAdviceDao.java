package ua.com.fielden.platform.example.entities;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * An interface defining a contract for Advice DAO.
 *
 * @author 01es
 *
 */
public interface IAdviceDao extends IEntityDao<Advice> {
    /**
     * Retrieves advices active for the specified workshop using the definition of "active" as determined in {@link Advice#isActiveForContractor()} and {@link Advice#isActiveForPnl()}.
     *
     * @param workshop
     * @return
     */
    List<Advice> findActiveFor(final Workshop workshop);
}
