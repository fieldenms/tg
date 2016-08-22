package ua.com.fielden.platform.web.layout.api;

public interface IFlex {

    IQuantifier flex();

    IQuantifier flex(int ratio);

    IQuantifier flexNone();

    IQuantifier flexAuto();
}
