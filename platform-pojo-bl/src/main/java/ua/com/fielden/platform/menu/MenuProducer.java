package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.security.user.IUserProvider;

import com.google.inject.Inject;

public class MenuProducer implements IEntityProducer<Menu> {

    private final IMenuRetriever menuRetirever;
    private final IUserProvider userProvider;

    @Inject
    public MenuProducer(final IMenuRetriever menuRetirever, final IUserProvider userProvider) {
        this.menuRetirever = menuRetirever;
        this.userProvider = userProvider;
    }

    @Override
    public Menu newEntity() {
        return menuRetirever.getMenuEntity().setCanEdit(userProvider.getUser().isBase());
    }

}
