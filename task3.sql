connect 'jdbc:derby:S3749857A1;create=true';

CREATE TABLE TASK3(
id INT NOT NULL GENERATED ALWAYS AS IDENTITY,
person_name VARCHAR(80),
birth_place VARCHAR(200),
genre VARCHAR(390),
movement VARCHAR(225),
occupation VARCHAR(250),
PRIMARY KEY(id));


CALL SYSCS_UTIL.SYSCS_SET_RUNTIMESTATISTICS(1);
CALL SYSCS_UTIL.SYSCS_SET_STATISTICS_TIMING(1);
MaximumDisplayWidth 2000;
CALL SYSCS_UTIL.SYSCS_IMPORT_DATA(null,'TASK3','PERSON_NAME,BIRTH_PLACE,GENRE,MOVEMENT,OCCUPATION','2,26,53,72,81','artist.csv',null,null,null,0);
values SYSCS_UTIL.SYSCS_GET_RUNTIMESTATISTICS();