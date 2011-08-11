package ua.com.fielden.platform.example.dnd.classes;

import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.pmodels.ContainerRetriever;

public interface RotableContainerRetriever extends ContainerRetriever {

    Rotable getRotable();

}
