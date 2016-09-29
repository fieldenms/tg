package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.dao.IEntityProducer;

import com.google.inject.Inject;

public class MenuProducer implements IEntityProducer<Menu> {

    private final IMenuRetriever menuRetirever;

    @Inject
    public MenuProducer(final IMenuRetriever menuRetirever) {
        this.menuRetirever = menuRetirever;
    }

    @Override
    public Menu newEntity() {
        return menuRetirever.getMenuEntity();
    }

}
