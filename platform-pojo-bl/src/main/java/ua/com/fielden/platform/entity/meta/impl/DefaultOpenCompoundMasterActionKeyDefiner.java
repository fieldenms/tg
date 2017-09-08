package ua.com.fielden.platform.entity.meta.impl;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;
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
 * <li>for persisted entity -- "[key]: [desc]" or just "[key]" if description is empty
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
            : (
              isEmpty(key.getDesc())
              ? format("%s", entityTitleObject(key))
              : format("%s: %s", entityTitleObject(key), key.getDesc())
            );
        property.getEntity().set("sectionTitle", sectionTitleValue);
    }
    
    /**
     * Override this method to provide custom entity formatting in section title.
     * 
     * @param entity
     * @return
     */
    protected Object entityTitleObject(final AbstractEntity<?> entity) {
        return entity.getKey();
    }
    
}
