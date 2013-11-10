DROP DATABASE remote:localhost/mgdb admin admin;
CREATE DATABASE remote:localhost/mgdb admin admin plocal;
connect remote:localhost/mgdb admin admin;

create class person;
create property person.pID INTEGER;
create property person.name STRING;
create property person.onlineDescendants INTEGER;
create index person.pID unique_hash_index;

create class dissertation;
create property dissertation.dID INTEGER;
create property dissertation.author LINK person;
create property dissertation.title STRING;
create property dissertation.university STRING;
create property dissertation.year STRING;
create index dissertation.dID notunique_hash_index;

create class advised;
create property advised.dissertation LINK dissertation;
create property advised.advisorOrder INTEGER;
alter property advised.advisorOrder MIN 1;
create property advised.advisor LINK person;
create index iadviseInfo on advised (dissertation, advisor) unique_hash_index;

disconnect;




