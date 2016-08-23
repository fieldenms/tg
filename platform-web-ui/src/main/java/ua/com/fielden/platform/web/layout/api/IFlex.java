package ua.com.fielden.platform.web.layout.api;

public interface IFlex extends ISelect {

    ISelect flex();

    ISelect flex(int ratio);

    ISelect flexNone();

    ISelect flexAuto();
}
