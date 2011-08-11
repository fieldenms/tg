package ua.com.fielden.platform.swing.egi.coloring;

import java.awt.Color;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.builders.AbstractTableModelBuilder;

/**
 * Interface representing, as the name implies, colouring scheme for EGI.
 * 
 * @author yura
 * 
 * @see AbstractTableModelBuilder#setEgiColoringScheme(EgiColoringScheme)
 * @param <T>
 */
public interface IEgiColoringScheme<T extends AbstractEntity> {

    /**
     * This method will be invoked when painting cell for given property of a given entity.<br>
     * If this method returns non-null value, cell's background will be painted with returned {@link Color} instance.<br>
     * If this method returns null, nothing will happen.<br>
     * <br>
     * Please also note that if property has validation error/warning, property will be painted with red/yellow colour no matter what {@link Color} is returned.
     * 
     * @param entity
     * @param propertyName
     * @return
     */
    Color getBgColour(T entity, String propertyName);

}
