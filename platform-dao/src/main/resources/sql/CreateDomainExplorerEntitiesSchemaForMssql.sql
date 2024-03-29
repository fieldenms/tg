IF OBJECT_ID('DOMAINPROPERTY_', 'U') IS NOT NULL DROP TABLE DOMAINPROPERTY_;
GO

IF OBJECT_ID('DOMAINTYPE_', 'U') IS NOT NULL DROP TABLE DOMAINTYPE_;
GO

CREATE TABLE DOMAINTYPE_
(
   _ID BIGINT PRIMARY KEY NOT NULL,
   KEY_ VARCHAR(255) NOT NULL,
   DESC_ VARCHAR(255) NOT NULL,
   DBTABLE_ VARCHAR(255),
   ENTITYTYPEDESC_ VARCHAR(255),
   ENTITY_ CHAR(1) DEFAULT 'N',
   PROPSCOUNT_ INT DEFAULT ((0)) NOT NULL,
   _VERSION BIGINT DEFAULT ((0)) NOT NULL
);
GO

CREATE TABLE DOMAINPROPERTY_
(
   _ID BIGINT PRIMARY KEY NOT NULL,
   NAME_ VARCHAR(255) NOT NULL,
   TITLE_ VARCHAR(255),
   DESC_ VARCHAR(1024),
   HOLDER__DOMAINTYPE BIGINT,
   HOLDER__DOMAINPROPERTY BIGINT,
   DOMAINTYPE_ BIGINT NOT NULL,
   KEYINDEX_ INT,
   REQUIRED_ CHAR(1) DEFAULT 'N',
   DBCOLUMN_ VARCHAR(255),
   POSITION_ INT NOT NULL,
   _VERSION BIGINT DEFAULT ((0)) NOT NULL
);
GO

CREATE INDEX I_DOMAINPROPERTY__HOLDER__DOMAINTYPE_ ON DOMAINPROPERTY_(HOLDER__DOMAINTYPE);
GO

CREATE INDEX I_DOMAINPROPERTY__HOLDER__DOMAINPROPERTY_ ON DOMAINPROPERTY_(HOLDER__DOMAINPROPERTY);
GO
