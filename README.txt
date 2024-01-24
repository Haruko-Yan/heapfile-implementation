Task 1
For execuating the program, command format shows below:

java dbload -p <page size> <data file path>

For example, if artist.csv is in the same folder of dbload.java and you want page size to be 4096, the command will be:

java dbload -p 4096 artist.csv

Tips:
1. The recommanded page size is 4096 or greater than 4096. The page size must be equal to or greater than 1592.
2. The heap file will be produced in the same folder of the dbload.java file.

Task 2
For execuating the program, command format shows below:

java birthDate <heap file path> <start date> <end date>

For example, if you want to list all artists born in 1970 and the heap file is in the same folder of birthDate.java and its name is heap.4096, the command will be:

java birthDate heap.4096 19700101 19701230

Tips:
1. The heap file must be the heap file produced by the program of dbload.java.

Task 3
Firstly, use the command below to pre-process the data set:

sed -i '1,4d' artist.csv

Then, using the command below to enter ij tool:

java org.apache.derby.tools.ij

In the ij tool, you should enter the command below to execuate the SQL script to load the data set(the artist.csv and task3.sql must be in the current folder):

run 'task3.sql';

First query:

SELECT COUNT(*) FROM TASK3 
WHERE UPPER(MOVEMENT) LIKE '%POST-IMPRESSIONISM%';

Second query:

SELECT PERSON_NAME FROM TASK3
WHERE UPPER(GENRE) LIKE '%PUNK ROCK%' AND
UPPER(OCCUPATION) LIKE '%MUSICIAN%' AND
UPPER(BIRTH_PLACE) LIKE '%AUSTRALIA%';
