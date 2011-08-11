package ua.com.fielden.platform.pmodels;

import java.awt.Color;

import ua.com.fielden.uds.designer.zui.interfaces.IContainer;

public interface ContainerRetriever {

    IContainer getContainer();

    void setContainer(IContainer container);

    void fill(Color color);
}
