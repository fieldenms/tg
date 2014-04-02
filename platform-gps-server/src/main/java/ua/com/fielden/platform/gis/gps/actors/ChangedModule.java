package ua.com.fielden.platform.gis.gps.actors;

import ua.com.fielden.platform.gis.gps.AbstractAvlModule;

public class ChangedModule<MODULE extends AbstractAvlModule> extends Changed<MODULE> {

    public ChangedModule(final MODULE value) {
        super(value);
    }

}
