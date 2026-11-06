-- create_insert_statement
--
-- PostgreSQL procedure that returns INSERT statements for the rows of a table.
--
-- Each INSERT statement spans a single line.
-- Multi-line values are represented as escape strings with the line terminators escaped.
--
-- Call the function like this:
--
-- CREATE TABLE mytable(a INT, b INT);
-- INSERT INTO mytable VALUES (0, -999);
--
-- SELECT create_insert_statement(tableoid, mytable) FROM mytable;
--
-- returns
--            create_insert_statement
-------------------------------------------------
-- INSERT INTO mytable (a,b) VALUES ('0','-999')
--(1 row)
--
-- Based off the code at:
-- https://wiki.postgresql.org/wiki/Create_INSERT_statement

CREATE OR REPLACE FUNCTION create_insert_statement(regclass, anyelement) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
_schemaname text;
_tablename text;
_key text;
_value text;
_literal text;
_columns text[];
_values text[];
BEGIN
SELECT pg_namespace.nspname, pg_class.relname
INTO STRICT _schemaname, _tablename
FROM pg_class
JOIN pg_namespace
    ON (pg_namespace.oid = pg_class.relnamespace)
WHERE pg_class.oid = $1;

FOR _key IN
    SELECT COLUMNS.column_name
    FROM information_schema.COLUMNS
    WHERE COLUMNS.table_schema = _schemaname
      AND COLUMNS.TABLE_NAME = _tablename
    ORDER BY COLUMNS.ordinal_position
LOOP
    EXECUTE format($s$SELECT ((($1)::%s).%I)::text$s$, $1, _key)
    USING $2 INTO STRICT _value;

    IF _value IS NULL THEN
        _literal := 'NULL';
    ELSE
        -- E'' escape string; backslash must be doubled first
        -- '' within a string represents the single quote character.
        _literal := 'E''' || replace(replace(replace(replace(replace(
            _value,
            '\',   '\\'),
            '''',  '\'''),
            E'\r', '\r'),
            E'\n', '\n'),
            E'\t', '\t') || '''';
    END IF;

    _columns := _columns || quote_ident(_key);
    _values  := _values  || _literal;
END LOOP;
RETURN format('INSERT INTO %s (%s) VALUES (%s)',
              $1, array_to_string(_columns, ','), array_to_string(_values, ','));
END;
$$;
