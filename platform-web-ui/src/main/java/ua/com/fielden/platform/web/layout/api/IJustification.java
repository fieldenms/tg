package ua.com.fielden.platform.web.layout.api;

public interface IJustification extends IAlignment {

    IAlignment startJustified();

    IAlignment centerJustified();

    IAlignment endJustified();

    IAlignment justified();

    IAlignment aroundJustified();

}
