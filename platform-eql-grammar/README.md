
### ANTLR

Command that generates ANTLR artifacts (including parser and lexer):

```
mvn antlr4:antlr4
```

Generated artifacts should be stored in `platform-pojo-bl:src/main/java/:ua.com.fielden.platform.eql.antlr`.
If this is not the case, review the plugin configuration in `pom.xml`. Look for the `outputDirectory` parameter.

References:

* [ANTLR Maven plugin home page](https://www.antlr.org/api/maven-plugin/latest/usage.html)
