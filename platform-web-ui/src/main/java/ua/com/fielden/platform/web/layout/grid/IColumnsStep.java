package ua.com.fielden.platform.web.layout.grid;

/// The configuration step at which a grid's column tracks are about to be declared.
/// The only available action is [#columns()], which begins the column-track declaration.
///
public interface IColumnsStep {

    /// Begins declaration of the grid's column tracks.
    ///
    IColumns columns();
}
