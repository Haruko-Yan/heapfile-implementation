import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class birthDate {

	public static void main(String[] args) throws ParseException, IOException {
		
		int recordSize = 1592; // set record size
		int readBytes = 0; // the number of bytes that has been already read in the current page
		int matchedRecordCounter = 0; // for counting the number of matched records
		
		
		String filePath = args[0]; // name of the heap file
		// Extract the page size from the file name
		String[] temp = filePath.split("\\.");
		// Select the last part of the splitting as page size in case there is '.' in the path of the heap file
		int pageSize = Integer.parseInt(temp[temp.length - 1]);
		byte[] page = new byte[pageSize];
		// Get the start date and end date from the command line
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
		long startMillis = ft.parse(args[1]).getTime(); // milliseconds of the start date
		long endMillis = ft.parse(args[2]).getTime(); // milliseconds of the end date
		// The birth date field takes up 16 bytes in the heap file. If there are two birth days, the last 8 bytes
		// should be non-zero value
		byte[] firstDate = new byte[8]; // first birth day
		byte[] diff = new byte[8]; // the milliseconds between the first and second date
		
		FileInputStream in = null;
		
		try {
			in = new FileInputStream(filePath);
			long startTime = System.currentTimeMillis();
			while ((in.read(page)) != -1) {
				readBytes = 0;
				while (pageSize - readBytes > recordSize) {
					// birthDate start at 60th byte of every record
					for (int i = 0; i < firstDate.length; i++) {
						firstDate[i] = page[i + readBytes + 60];
					}
					long date1 = bytesToLong(firstDate);
					long different = 0;
					// If the value is -1, it represent the birth date is not recorded or is recorded not reasonably,
					// we skip it and process the next record
					if (date1 != -1) {
						int check = 0; // for checking whether the current record matches the range
						if (date1 >= startMillis && date1 <= endMillis) {
							check = 1;
						}
						// Check if there is another birth date and if any, check if it matches the range
						else {
							for (int i = 0; i < diff.length; i++) {
								diff[i] = page[i + readBytes + 68];
							}
							different = bytesToLong(diff);
							// If there is another birth date, check if it matches the range
							if (different != 0) {
								long date2 = date1 + different;
								if (date2 >= startMillis && date2 <= endMillis) {
									check = 1;
								}
							}
						}
						
						// If it matches the range, extract each field and print them to stdout
						if (check == 1) {
							matchedRecordCounter++;
							System.out.print(matchedRecordCounter + ". ");
							// personName
							int counter = 0; // for counting the length of non-null bytes
							for (int i = 0; i < 60; i++) {
								if (page[i + readBytes] == 0) {
									break;
								}
								counter++;
							}
							// If it's all 0 values of the bytes in this field, print 'NULL' to stdout
							if (counter == 0) {
								System.out.print("NULL" + "\t");
							}
							else {
								// Extract the String of personName
								String personName = new String(page, readBytes, counter);
								System.out.print(personName + "\t");
							}
							
							// birthDate
							SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
							if (different == 0) {
								System.out.print(sFormat.format(new Date(date1)) + "\t");
							}
							else {
								long date2 = date1 + different;
								System.out.print("{" + sFormat.format(new Date(date1)) + " | " + 
										sFormat.format(new Date(date2)) + "}" + "\t");
							}
							
							// birthPlace label
							counter = 0; // initialize the counter
							for (int i = 0; i < 150; i++) {
								if (page[i + readBytes + 76] == 0) {
									break;
								}
								counter++;
							}
							// If it's all 0 values of the bytes in this field, print 'NULL' to stdout
							if (counter == 0) {
								System.out.print("NULL" + "\t");
							}
							else {
								// Extract the String of birthPlace
								String birthPlace = new String(page, readBytes + 76, counter);
								System.out.print(birthPlace + "\t");
							}
							
							// deathDate (same as birthDate)
							for (int i = 0; i < firstDate.length; i++) {
								firstDate[i] = page[i + readBytes + 226];
							}
							date1 = bytesToLong(firstDate);
							different = 0;
							// If the value is -1, it represent the birth date is not recorded or is recorded not reasonably,
							// we skip it and process the next record
							if (date1 == -1) {
								System.out.print("NULL" + "\t");
							}
							if (date1 != -1) {
								for (int i = 0; i < diff.length; i++) {
									diff[i] = page[i + readBytes + 234];
								}
								different = bytesToLong(diff);
								if (different == 0) {
									System.out.print(sFormat.format(new Date(date1)) + "\t");
								}
								else {
									long date2 = date1 + different;
									System.out.print("{" + sFormat.format(new Date(date1)) + " | " + 
											sFormat.format(new Date(date2)) + "}" + "\t");
								}
							}
							
							// Print the next 5 fields to stdout
							int[] position = {242, 442, 672, 928, 978, 1253}; // the location of the next six fields in the record
							for (int i = 0; i < position.length - 1; i++) {
								counter = 0; // initialize the counter
								for (int j = 0; j < position[i + 1] - position[i]; j++) {
									if (page[j + readBytes + position[i]] == 0) {
										break;
									}
									counter++;
								}
								// If it's all 0 values of the bytes in this field, print 'NULL' to stdout
								if (counter == 0) {
									System.out.print("NULL" + "\t");
								}
								else {
									// Extract the String of birthPlace
									String field = new String(page, readBytes + position[i], counter);
									System.out.print(field + "\t");
								}
							}
							
							// wikiPageID
							byte[] wikiPageID = new byte[4];
							for (int i = 0; i < wikiPageID.length; i++) {
								wikiPageID[i] = page[i + readBytes + 1253];
							}
							System.out.print(bytesToInt(wikiPageID) + "\t");
							
							// description
							counter = 0; // initialize the counter
							for (int i = 0; i < 335; i++) {
								if (page[i + readBytes + 1257] == 0) {
									break;
								}
								counter++;
							}
							// If it's all 0 values of the bytes in this field, print 'NULL' to stdout
							if (counter == 0) {
								System.out.print("NULL" + "\t");
							}
							else {
								// Extract the String of birthPlace
								String birthPlace = new String(page, readBytes + 1257, counter);
								System.out.println(birthPlace);
							}
						}
					}
					readBytes += recordSize;
				}
				
			}
			long endTime = System.currentTimeMillis();
			System.out.println("The number of milliseconds to do this query:" + (endTime - startTime));
			
		} catch (FileNotFoundException e) {
			System.out.println("please enter correct location of the data file");
		} finally {
			in.close();
		}
		
		
		
	}
	
	/**
	 * convert byte array to long
	 */
	public static long bytesToLong(byte[] array) {
        long value = 0;
        for (int i = 0; i < array.length ; i++) {
            value |= ((long)(array[i] & 0xff) << ((array.length - i - 1) * 8));
        }
        return value;
    }
	
	/**
	 * convert byte array to int
	 */
	public static int bytesToInt(byte[] array) {
        int value = 0;
        for (int i = 0; i < array.length ; i++) {
            value |= ((array[i] & 0xff) << ((array.length - i - 1) * 8));
        }
        return value;
    }

}
