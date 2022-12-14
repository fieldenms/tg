package ua.com.fielden.platform.eql.stage2;

import static java.util.Map.copyOf;

import java.util.Map;

import ua.com.fielden.platform.eql.stage2.sources.enhance.TreeResult;
import ua.com.fielden.platform.eql.stage3.Table;

public record TablesAndSourceTreeResult (Map<String, Table> tables, TreeResult treeResult) {
    public TablesAndSourceTreeResult {
        tables = copyOf(tables);
    }
}