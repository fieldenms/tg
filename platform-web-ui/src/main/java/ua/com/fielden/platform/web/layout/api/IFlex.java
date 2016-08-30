package ua.com.fielden.platform.web.layout.api;

public interface IFlex extends ILayoutCellCompleted {

    ILayoutCellCompleted flex();

    ILayoutCellCompleted flex(int ratio);

    ILayoutCellCompleted flexNone();

    ILayoutCellCompleted flexAuto();
}
