package ua.com.fielden.platform.entity;

/// There are situations where an entity, especially actions or synthetic, does not have a suitable key.
/// Often in such cases either `String` or `DynamicEntityKey` is specified, which reduced the clarity of the intended model.
///
/// It would be good if type [Void] were applicable, but it does not implement [Comparable], which is required for entity keys.
///
/// That is why type [NoKey], which may have single instance `NO_KEY`, is introduced and should be used as a key type parameter for entities that do not really need a key per se.
/// The key value `NO_KEY` is needed to be used as the `key` property is required by definition.
///
public enum NoKey implements Comparable<NoKey> {
    NO_KEY;
}
