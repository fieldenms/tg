package ua.com.fielden.platform.entity.meta.impl;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.CompoundMasterException;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

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
            throw new CompoundMasterException(String.format("Illegal empty (null) key has been set for [%s] action.", property.getEntity().getType().getSimpleName())); 
        }
        
        final String sectionTitleValue = !key.isPersisted() 
            ? "Add new " + TitlesDescsGetter.getEntityTitleAndDesc(key.getType()).getKey()
            : format("%s: %s", key.getKey(), key.getDesc()); // TODO please handle 'null' descriptions not to be shown as "KEY1: null"
        property.getEntity().set("sectionTitle", sectionTitleValue);
    }
    
}
