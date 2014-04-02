package ua.com.fielden.platform.swing.schedule;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract that allows to specify domain axis lable for entity.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IDomainLabelGenerator<T extends AbstractEntity<?>> {

    /**
     * Returns the label for domain axis label that corresponds to the specified entity.
     * 
     * @param entity
     * @return
     */
    String getDoaminName(T entity);
}