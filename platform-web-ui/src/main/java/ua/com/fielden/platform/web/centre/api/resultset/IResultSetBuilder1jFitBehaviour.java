package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract that allows one to specify whether egi content fits to its height or not.
///
public interface IResultSetBuilder1jFitBehaviour<T extends AbstractEntity<?>> extends IResultSetBuilder1kRowHeight<T> {

    /// Makes egi content fit to its height.
    ///
    /// The egi of a centre in a master component, such as one embedded with `tg-master-centre`, always fits to its height, filling the component, whether or not this method is called,
    /// and neither `setHeight` nor `setVisibleRowsCount` applies to it.
    ///
    IResultSetBuilder1kRowHeight<T> fitToHeight();
}
