package ua.com.fielden.platform.swing.egi;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface ITotalCalculator<T, E extends AbstractEntity<?>> {

    T calculate(List<E> entities);

    String getDescription();
}
