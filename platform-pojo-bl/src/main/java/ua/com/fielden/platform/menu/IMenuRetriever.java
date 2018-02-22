package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.web.interfaces.DeviceProfile;

public interface IMenuRetriever {
    
    /**
     * Returns a {@link Menu} entity for concrete {@link DeviceProfile}.
     * 
     * @param deviceProfile
     * @return
     */
    Menu getMenuEntity(final DeviceProfile deviceProfile);
    
}
