package ua.com.fielden.platform.entity.meta.impl;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.CompoundMasterException;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * Default implementation for compound master opener's key property.
 * <p> 
 * Handles updates to section title:
 * <p><ul>
 * <li>for new entity -- "Add new [Title Of Entity]"
 * <li>for persisted entity -- "[key]: [desc]"
 * </ul><p>
 *  
 * @author TG Team
 *
 */
public class DefaultOpenCompoundMasterActionKeyDefiner implements IAfterChangeEventHandler<AbstractEntity<?>> {
    
    @Override
    public void handle(final MetaProperty<AbstractEntity<?>> property, final AbstractEntity<?> key) {
        if (key == null) {
            throw new CompoundMasterException(format("Illegal empty (null) key has been set for [%s] action.", property.getEntity().getType().getSimpleName())); 
        }
        
        final String sectionTitleValue = !key.isPersisted() 
            ? "Add new " + getEntityTitleAndDesc(key.getType()).getKey()
            : format("%s: %s", key.getKey(), key.getDesc()); // TODO please handle 'null' descriptions not to be shown as "KEY1: null"
        property.getEntity().set("sectionTitle", sectionTitleValue);
    }
    
}
