package ua.com.fielden.platform.swing.review.report.interfaces;

public interface IDataLoader {

    void loadData();

    void addDataLoadedEventListener(IDataLoadedEventListener l);

    void removeDataLoadedEventListener(IDataLoadedEventListener l);

    void addPageLoadedEventListener(IPageLoadedEventListener l);

    void removePageLoadedEventListener(IPageLoadedEventListener l);
}