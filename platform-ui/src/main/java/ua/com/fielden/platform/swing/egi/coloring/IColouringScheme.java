package ua.com.fielden.platform.swing.egi.coloring;

import java.awt.Color;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for defining a custom colour schema for an EGI cell.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IColouringScheme<T extends AbstractEntity> {

    Color getColor(T entity);

}
