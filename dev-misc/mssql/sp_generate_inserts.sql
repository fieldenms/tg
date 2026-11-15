SET NOCOUNT ON
GO

PRINT 'Using Master database'
USE master
GO

PRINT 'Checking for the existence of this procedure'
IF (SELECT OBJECT_ID('sp_generate_inserts','P')) IS NOT NULL --means, the procedure already exists
    BEGIN
        PRINT 'Procedure already exists. So, dropping it'
        DROP PROC sp_generate_inserts
    END
GO

CREATE PROC sp_generate_inserts
(
    @table_name varchar(776),              -- The table/view for which the INSERT statements will be generated using the existing data
    @target_table varchar(776) = NULL,     -- Use this parameter to specify a different table name into which the data will be inserted
    @include_column_list bit = 1,          -- Use this parameter to include/omit column list in the generated INSERT statement
    @from varchar(800) = NULL,             -- Use this parameter to filter the rows based on a filter condition (using WHERE)
    @include_timestamp bit = 0,            -- Specify 1 for this parameter, if you want to include the TIMESTAMP/ROWVERSION column's data in the INSERT statement
    @debug_mode bit = 0,                   -- If @debug_mode is set to 1, the SQL statements constructed by this procedure will be printed for later examination
    @owner varchar(64) = NULL,             -- Use this parameter if you are not the owner of the table
    @omit_images bit = 0,                  -- Use this parameter to generate INSERT statements by omitting the 'image' columns
    @omit_identity bit = 0,                -- Use this parameter to omit the identity columns
    @top int = NULL,                       -- Use this parameter to generate INSERT statements only for the TOP n rows
    @cols_to_include varchar(8000) = NULL, -- List of columns to be included in the INSERT statement
    @cols_to_exclude varchar(8000) = NULL, -- List of columns to be excluded from the INSERT statement
    @disable_constraints bit = 0,          -- When 1, disables foreign key constraints and enables them after the INSERT statements
    @omit_computed_cols bit = 0            -- When 1, computed columns will not be included in the INSERT statement

)
AS
BEGIN

/***********************************************************************************************************
Procedure: sp_generate_inserts  (Build 22)
           (Copyright © 2002 Narayana Vyas Kondreddi. All rights reserved.)

Purpose: To generate INSERT statements from existing data.
         These INSERTS can be executed to regenerate the data at some other location.
         This procedure is also useful to create a database setup, where in you can
         script your data along with your table definitions.

Written by: Narayana Vyas Kondreddi
            http://vyaskn.tripod.com

            with edits by the TG team:
            * Column metadata is read in a single set-based query from sys.columns by object_id,
              instead of a per-column loop over INFORMATION_SCHEMA.COLUMNS (requires SQL Server 2017+ for STRING_AGG).
            * (n)char/(n)varchar and binary columns of up to 333 bytes are scripted without a conversion to (max).
            * Identity and computed columns are detected correctly when the caller is not dbo (e.g. a test login).
            * SET IDENTITY_INSERT targets @target_table when it is specified.
            * Correct literals for date, time, datetimeoffset (quoted ISO 8601), binary/varbinary (0x...),
              xml and json (quoted), wide decimal/numeric values, and float/real (round-trip precision).
            * Trailing spaces in varchar/nvarchar values are preserved.
            * An empty table returns an empty result set straight away, without the informational messages.

Acknowledgements:
        Divya Kalra -- For beta testing
        Mark Charsley -- For reporting a problem with scripting uniqueidentifier columns with NULL values
        Artur Zeygman -- For helping me simplify a bit of code for handling non-dbo owned tables
        Joris Laperre   -- For reporting a regression bug in handling text/ntext columns

Tested on: SQL Server 7.0 and SQL Server 2000 and SQL Server 2005

Date created: January 17th 2001 21:52 GMT

Date modified: May 1st 2002 19:50 GMT

Email:   vyaskn@hotmail.com

NOTE: sql_variant, hierarchyid, geometry, geography and other types not listed above are scripted as quoted
      Unicode strings, relying on implicit conversion; spatial values lose their SRID.

Example 1:  To generate INSERT statements for table 'titles':

            EXEC sp_generate_inserts 'titles'

Example 2:  To omit the column list in the INSERT statement: (Column list is included by default)
            IMPORTANT: If you have too many columns, you are advised to omit column list, as shown below,
            to avoid erroneous results

            EXEC sp_generate_inserts 'titles', @include_column_list = 0

Example 3:  To generate INSERT statements for 'titlesCopy' table from 'titles' table:

            EXEC sp_generate_inserts 'titles', 'titlesCopy'

Example 4:  To generate INSERT statements for 'titles' table for only those titles
            which contain the word 'Computer' in them:
            NOTE: Do not complicate the FROM or WHERE clause here. It's assumed that you are good with T-SQL if you are using this parameter

            EXEC sp_generate_inserts 'titles', @from = "from titles where title like '%Computer%'"

Example 5:  To specify that you want to include TIMESTAMP column's data as well in the INSERT statement:
            (By default TIMESTAMP column's data is not scripted)

            EXEC sp_generate_inserts 'titles', @include_timestamp = 1

Example 6:  To print the debug information:

            EXEC sp_generate_inserts 'titles', @debug_mode = 1

Example 7:  If you are not the owner of the table, use @owner parameter to specify the owner name
            To use this option, you must have SELECT permissions on that table

            EXEC sp_generate_inserts Nickstable, @owner = 'Nick'

Example 8:  To generate INSERT statements for the rest of the columns excluding images
            When using this otion, DO NOT set @include_column_list parameter to 0.

            EXEC sp_generate_inserts imgtable, @omit_images = 1

Example 9:  To generate INSERT statements excluding (omiting) IDENTITY columns:
            (By default IDENTITY columns are included in the INSERT statement)

            EXEC sp_generate_inserts mytable, @omit_identity = 1

Example 10: To generate INSERT statements for the TOP 10 rows in the table:

            EXEC sp_generate_inserts mytable, @top = 10

Example 11: To generate INSERT statements with only those columns you want:

            EXEC sp_generate_inserts titles, @cols_to_include = "'title','title_id','au_id'"

Example 12: To generate INSERT statements by omitting certain columns:

            EXEC sp_generate_inserts titles, @cols_to_exclude = "'title','title_id','au_id'"

Example 13: To avoid checking the foreign key constraints while loading data with INSERT statements:

            EXEC sp_generate_inserts titles, @disable_constraints = 1

Example 14: To exclude computed columns from the INSERT statement:
            EXEC sp_generate_inserts MyTable, @omit_computed_cols = 1
***********************************************************************************************************/

SET NOCOUNT ON

-- Omit computed columns when the source table is also the destination.
-- SQL Server rejects an INSERT statement if it includes a computed column.
IF @target_table IS NULL OR @target_table = @table_name
    BEGIN
        SET @omit_computed_cols = 1;
    END

--Making sure user only uses either @cols_to_include or @cols_to_exclude
IF ((@cols_to_include IS NOT NULL) AND (@cols_to_exclude IS NOT NULL))
    BEGIN
        RAISERROR('Use either @cols_to_include or @cols_to_exclude. Do not use both the parameters at once',16,1)
        RETURN -1 --Failure. Reason: Both @cols_to_include and @cols_to_exclude parameters are specified
    END

--Making sure the @cols_to_include and @cols_to_exclude parameters are receiving values in proper format
IF ((@cols_to_include IS NOT NULL) AND (PATINDEX('''%''',@cols_to_include COLLATE DATABASE_DEFAULT) = 0))
    BEGIN
        RAISERROR('Invalid use of @cols_to_include property',16,1)
        PRINT 'Specify column names surrounded by single quotes and separated by commas'
        PRINT 'Eg: EXEC sp_generate_inserts titles, @cols_to_include = "''title_id'',''title''"'
        RETURN -1 --Failure. Reason: Invalid use of @cols_to_include property
    END

IF ((@cols_to_exclude IS NOT NULL) AND (PATINDEX('''%''',@cols_to_exclude COLLATE DATABASE_DEFAULT) = 0))
    BEGIN
        RAISERROR('Invalid use of @cols_to_exclude property',16,1)
        PRINT 'Specify column names surrounded by single quotes and separated by commas'
        PRINT 'Eg: EXEC sp_generate_inserts titles, @cols_to_exclude = "''title_id'',''title''"'
        RETURN -1 --Failure. Reason: Invalid use of @cols_to_exclude property
    END


--Checking to see if the database name is specified along wih the table name
--Your database context should be local to the table for which you want to generate INSERT statements
--specifying the database name is not allowed
IF (PARSENAME(@table_name,3)) IS NOT NULL
    BEGIN
        RAISERROR('Do not specify the database name. Be in the required database and just specify the table name.',16,1)
        RETURN -1 --Failure. Reason: Database name is specified along with the table name, which is not allowed
    END

--Checking for the existence of 'user table' or 'view'
--This procedure is not written to work on system tables
--To script the data in system tables, just create a view on the system tables and script the view instead
--The object is resolved once; without @owner, the caller's default schema is used, the same as for the generated INSERTs.

DECLARE @Object_ID int,
        @Schema_Name sysname

SET @Object_ID = OBJECT_ID(CASE WHEN @owner IS NULL THEN '' ELSE QUOTENAME(LTRIM(RTRIM(@owner))) + '.' END + QUOTENAME(@table_name))

IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = @Object_ID AND type IN ('U','V'))
    BEGIN
        RAISERROR('User table or view not found.',16,1)
        PRINT 'You may see this error, if you are not the owner of this table or view. In that case use @owner parameter to specify the owner name.'
        PRINT 'Make sure you have SELECT permission on that table or view.'
        RETURN -1 --Failure. Reason: There is no user table or view with this name
    END

SET @Schema_Name = OBJECT_SCHEMA_NAME(@Object_ID)

--Returning an empty result set straight away for an empty table, which is common when scripting all tables of a database.
--The result set is the same as without this shortcut; only the informational messages are omitted.
IF @from IS NULL AND @disable_constraints = 0 AND OBJECTPROPERTY(@Object_ID, 'IsUserTable') = 1
    BEGIN
        DECLARE @Has_Rows bit = 0
        DECLARE @Has_Rows_Query nvarchar(max) = N'SELECT @Has_Rows = 1 WHERE EXISTS (SELECT 1 FROM ' + QUOTENAME(@Schema_Name) + N'.' + QUOTENAME(@table_name) + N' WITH (NOLOCK))'
        EXEC sp_executesql @Has_Rows_Query, N'@Has_Rows bit OUTPUT', @Has_Rows OUTPUT
        IF @Has_Rows = 0
            BEGIN
                SELECT CAST(NULL AS nvarchar(max)) WHERE 1 = 0
                RETURN 0
            END
    END

--Variable declarations
DECLARE @Column_List nvarchar(max),
        @Start_Insert nvarchar(1000),
        @Actual_Values nvarchar(max),   --This is the string that will be finally executed to generate INSERT statements
        @IDN sysname,                   --Will contain the IDENTITY column's name in the table
        @Inline_Max_Bytes int           --string and binary columns up to this size are scripted without converting to (max)

--Variable Initialization
SET @IDN = ''
SET @Column_List = ''
SET @Actual_Values = ''
-- A scripted value that is not (max) is converted to nvarchar(4000) when appended to the nvarchar(max) INSERT,
-- so it must fit in 4000 characters.
-- Escaping a line terminator turns 1 byte of varchar into 12 characters ('+CHAR(13)+'),
-- 2 bytes of nvarchar into 14 ('+NCHAR(13)+N'), and 1 byte of binary becomes 2 hex digits.
-- varchar is the worst case per byte: 333 bytes * 12 + 2 quotes = 3998 characters still fit.
SET @Inline_Max_Bytes = 333

IF @owner IS NULL
    BEGIN
        SET @Start_Insert = 'INSERT INTO ' + '[' + RTRIM(COALESCE(@target_table,@table_name)) + ']'
    END
ELSE
    BEGIN
        SET @Start_Insert = 'INSERT ' + '[' + LTRIM(RTRIM(@owner)) + '].' + '[' + RTRIM(COALESCE(@target_table,@table_name)) + ']'
    END


--Selecting the columns to script, in their ordinal order
DECLARE @Columns TABLE (Column_ID int PRIMARY KEY, Column_Name sysname, Data_Type sysname, Max_Length smallint, Is_Identity bit)

INSERT  @Columns (Column_ID, Column_Name, Data_Type, Max_Length, Is_Identity)
SELECT  c.column_id,
        c.name,
        CASE WHEN t.is_assembly_type = 1 THEN t.name ELSE TYPE_NAME(c.system_type_id) END, -- the base type for alias types, e.g. sysname -> nvarchar
        c.max_length,
        c.is_identity
FROM    sys.columns c
        JOIN sys.types t ON t.user_type_id = c.user_type_id
WHERE   c.object_id = @Object_ID
        AND (@cols_to_include IS NULL OR CHARINDEX('''' + c.name + '''', @cols_to_include) <> 0) --Selecting only user specified columns
        AND (@cols_to_exclude IS NULL OR CHARINDEX('''' + c.name + '''', @cols_to_exclude) = 0)
        AND (@omit_identity = 0 OR c.is_identity = 0)      --Determing whether to include or exclude the IDENTITY column
        AND (@omit_computed_cols = 0 OR c.is_computed = 0) --Making sure whether to output computed columns or not
        AND (@omit_images = 0 OR t.name <> 'image')

--Tables with columns of IMAGE data type are not supported for obvious reasons
IF EXISTS (SELECT 1 FROM @Columns WHERE Data_Type = 'image')
    BEGIN
        RAISERROR('Tables with image columns are not supported.',16,1)
        PRINT 'Use @omit_images = 1 parameter to generate INSERTs for the rest of the columns.'
        PRINT 'DO NOT omit Column List in the INSERT statements. If you omit column list using @include_column_list=0, the generated INSERTs will fail.'
        RETURN -1 --Failure. Reason: There is a column with image data type
    END

IF NOT EXISTS (SELECT 1 FROM @Columns)
    BEGIN
        RAISERROR('No columns to select. There should at least be one column to generate the output',16,1)
        RETURN -1 --Failure. Reason: Looks like all the columns are omitted using the @cols_to_exclude parameter
    END

--Making sure to output SET IDENTITY_INSERT ON/OFF in case the table has an IDENTITY column (not applicable to views)
SELECT @IDN = QUOTENAME(Column_Name) FROM @Columns WHERE Is_Identity = 1 AND OBJECTPROPERTY(@Object_ID, 'IsUserTable') = 1

--Determining the data type of the column and depending on the data type, the VALUES part of
--the INSERT statement is generated. Care is taken to handle columns with NULL values. Also
--making sure, not to lose any data from flot, real, money, smallmomey, datetime columns
SELECT  @Column_List = STRING_AGG(CONVERT(nvarchar(max), QUOTENAME(Column_Name)), ',') WITHIN GROUP (ORDER BY Column_ID),
        @Actual_Values = STRING_AGG(CONVERT(nvarchar(max),
            CASE l.Kind
                -- Quotes are doubled and line terminators are replaced by string concatenation.
                -- E.g., 'hello\nworld' -> 'hello' + char(10) + 'world'
                -- For Unicode values, NCHAR and N'...' keep the characters after a line terminator intact.
                -- For (n)varchar(max), text and ntext values, the concatenation starts with an empty (n)varchar(max),
                -- otherwise the concatenated literals are truncated at 8000 bytes when the INSERT is executed.
                WHEN 'S' THEN 'COALESCE(' + CASE WHEN n.Is_Max = 1 THEN '''CAST('''''''' AS varchar(max))+''''''' ELSE '''''''''' END + ' + REPLACE(REPLACE(REPLACE(' + v.Val + ','''''''',''''''''''''),CHAR(13),''''''+CHAR(13)+''''''),CHAR(10),''''''+CHAR(10)+'''''')+'''''''',''NULL'')'
                WHEN 'N' THEN 'COALESCE(' + CASE WHEN n.Is_Max = 1 THEN '''CAST(N'''''''' AS nvarchar(max))+N''''''' ELSE '''N''''''' END + ' + REPLACE(REPLACE(REPLACE(' + v.Val + ','''''''',''''''''''''),CHAR(13),''''''+NCHAR(13)+N''''''),CHAR(10),''''''+NCHAR(10)+N'''''')+'''''''',''NULL'')'
                WHEN 'Q' THEN 'COALESCE('''''''' + ' + v.Val + '+'''''''',''NULL'')'
                WHEN 'U' THEN 'COALESCE(' + v.Val + ',''NULL'')'
                ELSE v.Val
            END), '+'',''+') WITHIN GROUP (ORDER BY Column_ID)
FROM    @Columns
        CROSS APPLY (SELECT QUOTENAME(Column_Name) AS Col,
                            CASE WHEN Max_Length = -1 OR Max_Length > @Inline_Max_Bytes THEN 1 ELSE 0 END AS Is_Large,
                            CASE WHEN Max_Length = -1 OR Data_Type IN ('text','ntext') THEN 1 ELSE 0 END AS Is_Max) n
        CROSS APPLY (SELECT CASE
                                WHEN Data_Type IN ('char','varchar','text') THEN 'S'
                                WHEN Data_Type IN ('date','time','datetime','datetime2','smalldatetime','datetimeoffset','uniqueidentifier') THEN 'Q'
                                WHEN Data_Type IN ('bit','tinyint','smallint','int','bigint','decimal','numeric','float','real','money','smallmoney','binary','varbinary') THEN 'U'
                                WHEN Data_Type = 'timestamp' AND @include_timestamp = 1 THEN 'U'
                                WHEN Data_Type = 'timestamp' THEN 'D'
                                ELSE 'N' -- nchar, nvarchar, ntext, xml, json and anything else is scripted as a Unicode string
                            END AS Kind) l
        CROSS APPLY (SELECT CASE
                                WHEN Data_Type = 'char' AND Is_Large = 0 THEN 'RTRIM(' + Col + ') COLLATE DATABASE_DEFAULT'
                                WHEN Data_Type = 'char' THEN 'RTRIM(CONVERT(varchar(max),' + Col + ')) COLLATE DATABASE_DEFAULT'
                                WHEN Data_Type = 'nchar' AND Is_Large = 0 THEN 'RTRIM(' + Col + ') COLLATE DATABASE_DEFAULT'
                                WHEN Data_Type = 'nchar' THEN 'RTRIM(CONVERT(nvarchar(max),' + Col + ')) COLLATE DATABASE_DEFAULT'
                                WHEN Data_Type IN ('varchar','nvarchar') AND Is_Large = 0 THEN Col + ' COLLATE DATABASE_DEFAULT'
                                WHEN l.Kind = 'S' THEN 'CONVERT(varchar(max),' + Col + ') COLLATE DATABASE_DEFAULT'
                                WHEN l.Kind = 'N' THEN 'CONVERT(nvarchar(max),' + Col + ') COLLATE DATABASE_DEFAULT'
                                -- ISO 8601 (style 126) is independent of the session language and DATEFORMAT, and keeps full precision.
                                WHEN Data_Type = 'uniqueidentifier' THEN 'CONVERT(char(36),' + Col + ')'
                                WHEN l.Kind = 'Q' THEN 'CONVERT(varchar(40),' + Col + ',126)'
                                WHEN l.Kind = 'D' THEN '''DEFAULT'''
                                -- A 0x... literal; style 2 is hex without the prefix (style 1 returns '' rather than 0x for an empty varbinary(max)).
                                WHEN Data_Type IN ('binary','varbinary','timestamp') AND Is_Large = 1 THEN '''0x''+CONVERT(varchar(max),' + Col + ',2)'
                                WHEN Data_Type IN ('binary','varbinary','timestamp') THEN '''0x''+CONVERT(varchar(' + CONVERT(varchar(10), Max_Length * 2) + '),' + Col + ',2)'
                                -- Style 3 produces 17 significant digits, which round-trip exactly.
                                WHEN Data_Type IN ('float','real') THEN 'CONVERT(varchar(30),' + Col + ',3)'
                                WHEN Data_Type IN ('money','smallmoney') THEN 'CONVERT(varchar(30),' + Col + ',2)'
                                ELSE 'CONVERT(varchar(50),' + Col + ')'
                            END AS Val) v

--Forming the final string that will be executed, to output the INSERT statements
--The SELECT returns one INSERT per row, concatenated from the column values with +.
--+ is evaluated left to right, and a non-(max) intermediate result is silently truncated at 8000 bytes,
--so the INSERT text starts as nvarchar(max), which makes every intermediate result nvarchar(max).
--E.g., for table T (ID int, NAME varchar(50)), @Actual_Values becomes:
--    SELECT CONVERT(nvarchar(max),'INSERT INTO [T] ')+'([ID],[NAME]'+')' +'VALUES('+
--           COALESCE(CONVERT(varchar(50),[ID]),'NULL')
--           +','+
--           COALESCE('''' + REPLACE(REPLACE(REPLACE([NAME] COLLATE DATABASE_DEFAULT,'''',''''''),CHAR(13),'''+CHAR(13)+'''),CHAR(10),'''+CHAR(10)+''')+'''','NULL')
--           +')' FROM [T](NOLOCK)
--and for row (1, 'O''Brien') it returns:
--    INSERT INTO [T] ([ID],[NAME])VALUES(1,'O''Brien')
IF (@include_column_list <> 0)
    BEGIN
        SET @Actual_Values =
            'SELECT ' +
            CASE WHEN @top IS NULL OR @top < 0 THEN '' ELSE ' TOP ' + LTRIM(STR(@top)) + ' ' END +
            'CONVERT(nvarchar(max),''' + RTRIM(@Start_Insert) +
            ' '')+' + '''(' + RTRIM(@Column_List) +  '''+' + ''')''' +
            ' +''VALUES(''+ ' +  @Actual_Values  + '+'')''' + ' ' +
            COALESCE(@from,' FROM ' + CASE WHEN @owner IS NULL THEN '' ELSE '[' + LTRIM(RTRIM(@owner)) + '].' END + '[' + rtrim(@table_name) + ']' + '(NOLOCK)')
    END
ELSE IF (@include_column_list = 0)
    BEGIN
        SET @Actual_Values =
            'SELECT ' +
            CASE WHEN @top IS NULL OR @top < 0 THEN '' ELSE ' TOP ' + LTRIM(STR(@top)) + ' ' END +
            'CONVERT(nvarchar(max),''' + RTRIM(@Start_Insert) +
            ' '') +''VALUES(''+ ' +  @Actual_Values + '+'')''' + ' ' +
            COALESCE(@from,' FROM ' + CASE WHEN @owner IS NULL THEN '' ELSE '[' + LTRIM(RTRIM(@owner)) + '].' END + '[' + rtrim(@table_name) + ']' + '(NOLOCK)')
    END

--Determining whether to ouput any debug information
IF @debug_mode =1
    BEGIN
        PRINT '/*****START OF DEBUG INFORMATION*****'
        PRINT 'Beginning of the INSERT statement:'
        PRINT @Start_Insert
        PRINT ''
        PRINT 'The column list:'
        PRINT @Column_List
        PRINT ''
        PRINT 'The SELECT statement executed to generate the INSERTs'
        PRINT @Actual_Values
        PRINT ''
        PRINT '*****END OF DEBUG INFORMATION*****/'
        PRINT ''
    END

PRINT '--INSERTs generated by ''sp_generate_inserts'' stored procedure written by Vyas'
PRINT '--Build number: 22'
PRINT '--Problems/Suggestions? Contact Vyas @ vyaskn@hotmail.com'
PRINT '--http://vyaskn.tripod.com'
PRINT ''
PRINT 'SET NOCOUNT ON'
PRINT ''


--Determining whether to print IDENTITY_INSERT or not
IF (@IDN <> '')
    BEGIN
        PRINT 'SET IDENTITY_INSERT ' + QUOTENAME(@Schema_Name) + '.' + QUOTENAME(COALESCE(@target_table,@table_name)) + ' ON'
        PRINT 'GO'
        PRINT ''
    END


IF @disable_constraints = 1 AND OBJECTPROPERTY(@Object_ID, 'IsUserTable') = 1
    BEGIN
        IF @owner IS NULL
            BEGIN
                SELECT     'ALTER TABLE ' + QUOTENAME(COALESCE(@target_table, @table_name)) + ' NOCHECK CONSTRAINT ALL' AS '--Code to disable constraints temporarily'
            END
        ELSE
            BEGIN
                SELECT     'ALTER TABLE ' + QUOTENAME(@owner) + '.' + QUOTENAME(COALESCE(@target_table, @table_name)) + ' NOCHECK CONSTRAINT ALL' AS '--Code to disable constraints temporarily'
            END

        PRINT 'GO'
    END

PRINT ''
PRINT 'PRINT ''Inserting values into ' + '[' + RTRIM(COALESCE(@target_table,@table_name)) + ']' + ''''


--All the hard work pays off here!!! You'll get your INSERT statements, when the next line executes!
EXEC (@Actual_Values)

PRINT 'PRINT ''Done'''
PRINT ''


IF @disable_constraints = 1 AND OBJECTPROPERTY(@Object_ID, 'IsUserTable') = 1
    BEGIN
        IF @owner IS NULL
            BEGIN
                SELECT     'ALTER TABLE ' + QUOTENAME(COALESCE(@target_table, @table_name)) + ' CHECK CONSTRAINT ALL'  AS '--Code to enable the previously disabled constraints'
            END
        ELSE
            BEGIN
                SELECT     'ALTER TABLE ' + QUOTENAME(@owner) + '.' + QUOTENAME(COALESCE(@target_table, @table_name)) + ' CHECK CONSTRAINT ALL' AS '--Code to enable the previously disabled constraints'
            END

        PRINT 'GO'
    END

PRINT ''
IF (@IDN <> '')
    BEGIN
        PRINT 'SET IDENTITY_INSERT ' + QUOTENAME(@Schema_Name) + '.' + QUOTENAME(COALESCE(@target_table,@table_name)) + ' OFF'
        PRINT 'GO'
    END

PRINT 'SET NOCOUNT OFF'


SET NOCOUNT OFF
RETURN 0 --Success. We are done!
END

GO

PRINT 'Created the procedure'
GO


--Mark procedure as system object, so that it runs in the context of the calling database
EXEC sys.sp_MS_marksystemobject 'dbo.sp_generate_inserts'
GO

PRINT 'Granting EXECUTE permission on sp_generate_inserts to all users'
GRANT EXEC ON sp_generate_inserts TO public

SET NOCOUNT OFF
GO

PRINT 'Done'
