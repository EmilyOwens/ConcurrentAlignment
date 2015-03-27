create table dnaseqs.table1(
	id INTEGER NOT NULL,
	geneName VARCHAR(20) NOT NULL,
	sequence VARCHAR(20) NOT NULL,
	PRIMARY KEY (id)
);

INSERT into dnaseqs.table1 values (1, 'Test1', 'AlsoTestOne');