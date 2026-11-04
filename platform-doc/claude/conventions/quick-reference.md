# Conventions — Quick Reference

Conventions for structuring code that apply across topics.
Other general conventions are in the *Conventions* section of `CLAUDE.md`.

## Closed sets of names

When a fixed set of names (request parameters, property names, keys of a configuration group) is both referenced individually and checked as a whole, declare an enum whose members are named exactly as the names they represent (e.g. `client_id`, `clientId`).
Then `name()` and string formatting yield the name itself.

Give the enum a static lookup by name (`BY_NAME`) and the methods its users need, such as a membership test (`isRecognised`) or reading a member's value from a map (`valueIn`, `firstValueIn`).
A `String` constant per name plus a `Set` of all of them duplicates the names and can drift from them, whereas `values()` cannot.

```java
private enum ClientProperty {
    clientId,
    redirectUri,
    displayName;

    private static final Map<String, ClientProperty> BY_NAME = Stream.of(values()).collect(toUnmodifiableMap(ClientProperty::name, identity()));

    static boolean isRecognised(final String name) {
        return BY_NAME.containsKey(name);
    }

    @Nullable String valueIn(final Map<String, String> clientProps) {
        return clientProps.get(name());
    }
}
```

This takes precedence over *Grouped constants* in `CLAUDE.md` whenever the whole set is needed as a collection.
