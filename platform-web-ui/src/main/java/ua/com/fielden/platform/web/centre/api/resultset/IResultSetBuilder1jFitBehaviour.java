package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract that allows one to specify whether egi content fits to its height or not.
///
public interface IResultSetBuilder1jFitBehaviour<T extends AbstractEntity<?>> extends IResultSetBuilder1kRowHeight<T> {

    /// Makes egi content fit to its height.
    ///
    /// The egi fills the result view of the centre, covering any insertion points at the top, bottom, left or right of the centre, so it suits centres without such insertion points.
    /// In a centre embedded with `tg-centre-component`, the egi fills the component, stretched to its full height, rather than growing with its rows up to that height.
    ///
    IResultSetBuilder1kRowHeight<T> fitToHeight();
}
