package ua.com.fielden.platform.swing.booking;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Default implementation of the {@link ITooltipGenerator} interface.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class DefaultDomainLabelGenerator<T extends AbstractEntity<?>> implements IDomainLabelGenerator<T> {

    @Override
    public String getDoaminName(final T entity) {
        return entity.getKey().toString();
    }

}
