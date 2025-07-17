package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public interface ICompoundMaster6<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {

    /// Specify an instance of an unregistered embedded master that will serve as a view for this menu item.
    ///
    ICompoundMaster7<T, F> withView(final EntityMaster<?> embeddedMaster);

    /// Specify an instance of an unregistered embedded centre that will serve as a view for this menu item.
    ///
    ICompoundMaster7<T, F> withView(final EntityCentre<?> embeddedCentre);

    /// Specify a polymorphic embedded centre that will serve as a view for this menu item.
    ///
    ICompoundMaster7<T, F> withPolymorphicCenter();
}
