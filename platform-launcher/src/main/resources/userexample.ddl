CREATE USER IF NOT EXISTS SA SALT '68597c0f06e84117' HASH 'adbc2b70d7ff92396e46d1259eeb7a99f0e4516e10d8cd5b9b3a02d91ab5a510' ADMIN;
CREATE SEQUENCE IF NOT EXISTS COMMON_RMA_SEQUENCE WITH 10000;

CREATE TABLE PERSON (
	ID_COLUMN BIGINT NOT NULL,
	VERSION_COLUMN BIGINT NOT NULL DEFAULT 0,
	PERSON_NO VARCHAR(60) NOT NULL,
	PERSON_DESC VARCHAR(250) NOT NULL,
	PASSWORD VARCHAR(60) NOT NULL
);
INSERT INTO PERSON(ID_COLUMN, VERSION_COLUMN, PERSON_NO, PERSON_DESC, PASSWORD) VALUES
(1, 0, 'rma-user', 'RMA Default User', ''),
(2, 0, 'USR', 'Contractor User', ''); 

CREATE TABLE USER_ROLE (
	ID_COLUMN BIGINT NOT NULL,
	VERSION_COLUMN BIGINT NOT NULL DEFAULT 0,
	ROLE_NO VARCHAR(60) NOT NULL,
	ROLE_DESC VARCHAR(250) NOT NULL,
);
INSERT INTO USER_ROLE(ID_COLUMN, VERSION_COLUMN, ROLE_NO, ROLE_DESC) VALUES
(1, 0, 'admin', 'System administrator'),
(2, 0, 'guest', 'guest user'),
(3, 0, 'main', 'user role with a lot of roles'),
(4, 0, 'test', 'tester user role');

CREATE TABLE PERSON_ROLE_ASSOCIATION (
	ID_COLUMN BIGINT NOT NULL,
	VERSION_COLUMN BIGINT NOT NULL DEFAULT 0,
	PERSON_ID BIGINT NOT NULL,
	ROLE_ID BIGINT NOT NULL
);
INSERT INTO PERSON_ROLE_ASSOCIATION(ID_COLUMN, VERSION_COLUMN, PERSON_ID, ROLE_ID) VALUES
(1, 0, 1, 1),
(2, 0, 1, 2),
(3, 0, 2, 3),
(4, 0, 2, 4);

CREATE TABLE SECURITY_ASSOCIATION (
	ID_COLUMN BIGINT NOT NULL,
	VERSION_COLUMN BIGINT NOT NULL DEFAULT 0,
	TOKEN VARCHAR(250) NOT NULL,
	ROLE_ID BIGINT NOT NULL
);
INSERT INTO SECURITY_ASSOCIATION(ID_COLUMN, VERSION_COLUMN, TOKEN, ROLE_ID) VALUES
(1, 0, 'ua.com.fielden.platform.example.swing.securitytable.FirstLevelSecurityToken1', 1),
(2, 0, 'ua.com.fielden.platform.example.swing.securitytable.FirstLevelSecurityToken1', 2),
(3, 0, 'ua.com.fielden.platform.example.swing.securitytable.FirstLevelSecurityToken2', 3),
(4, 0, 'ua.com.fielden.platform.example.swing.securitytable.FirstLevelSecurityToken2', 4),
(5, 0, 'ua.com.fielden.platform.example.swing.securitytable.SecondLevelSecurityToken1', 1),
(6, 0, 'ua.com.fielden.platform.example.swing.securitytable.SecondLevelSecurityToken1', 2),
(7, 0, 'ua.com.fielden.platform.example.swing.securitytable.SecondLevelSecurityToken2', 3),
(8, 0, 'ua.com.fielden.platform.example.swing.securitytable.SecondLevelSecurityToken2', 4),
(9, 0, 'ua.com.fielden.platform.example.swing.securitytable.ThirdLevelSecurityToken1', 1),
(10, 0, 'ua.com.fielden.platform.example.swing.securitytable.ThirdLevelSecurityToken1', 2),
(11, 0, 'ua.com.fielden.platform.example.swing.securitytable.ThirdLevelSecurityToken2', 3),
(12, 0, 'ua.com.fielden.platform.example.swing.securitytable.ThirdLevelSecurityToken2', 4);

ALTER TABLE PERSON ADD CONSTRAINT CONSTRAINT_4_PERSONS PRIMARY KEY(ID_COLUMN);
ALTER TABLE USER_ROLE ADD CONSTRAINT CONSTRAINT_4_USER_ROLES PRIMARY KEY(ID_COLUMN);
ALTER TABLE PERSON_ROLE_ASSOCIATION ADD CONSTRAINT CONSTRAINT_4_ASSOCIATION PRIMARY KEY(ID_COLUMN);
ALTER TABLE SECURITY_ASSOCIATION ADD CONSTRAINT CONSTRAINT_4_SECURITY_ASSOCIATION PRIMARY KEY(ID_COLUMN);