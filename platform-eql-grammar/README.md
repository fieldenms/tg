
### ANTLR

Command that generates ANTLR artifacts (including parser and lexer):

```
mvn antlr4:antlr4
```

Generated artifacts should be stored in `platform-pojo-bl:src/main/java/:ua.com.fielden.platform.eql.antlr`.
If this is not the case, review the plugin configuration in `pom.xml`. Look for the `outputDirectory` parameter.

#### Troubleshooting

##### No grammars to process

If the ANTLR Maven plugin doesn't process the grammar file (`src/main/antlr4/EQL.g4`), "touch" the
file to change its modification date. The plugin is presumably trying to be smart and avoids doing
additional work if it had already processed the grammar file in the past.

References:

* [ANTLR Maven plugin home page](https://www.antlr.org/api/maven-plugin/latest/usage.html)
