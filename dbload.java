import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class dbload {
	/*
	 * 1. personName -- 60 bytes 2. birthDate -- 16 bytes(8 bytes for the first date, 8 bytes for the 
	 * second date(if possible)) 3. birthPlace label -- 150 bytes 4. deathDate -- 16 bytes(same as birthDate)
	 * 5. field label -- 200 bytes 6. genre label -- 230 bytes 7. instrument label -- 256 bytes
	 * 8. nationality label -- 50 bytes 9. thumbnail -- 275 bytes 10. wikiPageID -- 4 bytes
	 * 11. description -- 335 bytes
	 * 
	 * So the record size is 1592 bytes
	 * 
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader bf = null;
		FileOutputStream os = null;
		String[] fieldNames = {"rdf-schema#label", "birthDate", "birthPlace_label", "deathDate",
				"field_label", "genre_label", "instrument_label", "nationality_label", "thumbnail",
				"wikiPageID", "description"}; // the names of required fields
		String[] fields = new String[11]; // required fields
		int[] fieldIndex = new int[11]; // index of required fields in the dataset
		
		int pageSize = 0; // initialize page size
		int usedSpace = 0; // the space that has been already used in the current page
		int recordSize = 1592; // set record size
		int pageCounter = 0; // count the number of pages
		int recordCounter = 0; // count the number of all records
		String filePath = null; // path of the data file
		
		try {
			// Parse the parameter of the command line
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-p")) {
					pageSize = Integer.parseInt(args[i + 1]);
				}
			}
			if (pageSize < recordSize) {
				System.out.println("Record size is 1592 bytes, so page size must be greater than 1592 bytes");
			}
			else if (args.length <= 2) {
				System.out.println("Error command! Command should be 'java dbload -p pagesize datafile'");
			}
			else {
				// Get the path of the data file
				filePath = args[args.length - 1];
				byte[] page = new byte[pageSize]; // byte array of a page
				
				bf = new BufferedReader(new FileReader(filePath));
				// Read the first line and get all field names
				String line = bf.readLine();
				String[] allFieldNames = line.split("\",\"");
				// Find the index of required fields in the dataset
				for (int i = 0; i < allFieldNames.length; i++) {
					for (int j = 0; j < fieldNames.length; j++) {
						if (allFieldNames[i].equals(fieldNames[j])) {
							fieldIndex[j] = i;
						}
					}
				}
				// Skip the next three line since they are headers
				for (int i = 0; i < 3; i++) {
					bf.readLine();
				}
				
				// Delete the file with the same name and create the heap file
				File heapFile = new File("heap." + Integer.toString(pageSize));
				if (heapFile.exists()) {
					heapFile.delete();
				}
				heapFile.createNewFile();
				// The bytes will be written to the end of the heap file every time 
				os = new FileOutputStream(heapFile, true);
				
				long startTime = System.currentTimeMillis();
				// Traverse all rows
				while ((line = bf.readLine()) != null) {
					// If there is not enough space for the current page to store one more record, write 
					// the page to the binary file
					if (pageSize - usedSpace < recordSize) {
						os.write(page);
						pageCounter++;
						// Initialize the byte array of the page
						page = new byte[pageSize];
						// Zero out used space
						usedSpace = 0;
					}
					String[] allFields = line.split("\",\"");
					// Extract required fields by index we got before
					for (int i = 0; i < fieldIndex.length; i++) {
						fields[i] = allFields[fieldIndex[i]];
					}
					
					// Process and transfer every field to the current page
					
					// If there is null value, convert null value to String 'NULL' for ease of processing it
					for (int i = 0; i < fields.length; i++) {
						if (fields[i] == null) {
							fields[i] = "NULL";
						}
					}
					
					// personName
					byte[] personName = fields[0].getBytes();
					for (int i = 0; i < personName.length; i++) {
						page[i + usedSpace] = personName[i];
					}
					
					// birthDate
					String pattern = "(\\d{4}-\\d{2}-\\d{2})|(\\d{4}/\\d{2}/\\d{2})";
					Pattern p = Pattern.compile(pattern);
					Matcher matcher = p.matcher(fields[1]);
					// If no character match the pattern, the value may be not reasonable date or NULL,
					// so just convert them to -1 as presentation, and convert -1 to byte array for storing
					if (matcher.find() == false) {
						byte[] nonDate =  longToBytes(-1L);
						for (int i = 0; i < nonDate.length; i++) {
							page[i + usedSpace + 60] = nonDate[i];
						}
					}
					else {
						// Get the format of the date
						String dFormat1 = (matcher.group(1) != null)? "yyyy-MM-dd":"yyyy/MM/dd";
						String dataString1 = (matcher.group(1) != null)? matcher.group(1):matcher.group(2);
						SimpleDateFormat ft1 = new SimpleDateFormat(dFormat1);
						Date date1 = ft1.parse(dataString1);
						byte[] dateByte1 = longToBytes(date1.getTime());
						for (int i = 0; i < dateByte1.length; i++) {
							page[i + usedSpace + 60] = dateByte1[i];
						}
						// If there are two date in the field, calculate the milliseconds between the first
						// date and the second date, and store it using long and convert it to byte array
						// in order to store the milliseconds after the bytes of the first date
						if (matcher.find() == true) {
							String dFormat2 = (matcher.group(1) != null)? "yyyy-MM-dd":"yyyy/MM/dd";
							String dataString2 = (matcher.group(1) != null)? matcher.group(1):matcher.group(2);
							SimpleDateFormat ft2 = new SimpleDateFormat(dFormat2);
							Date date2 = ft2.parse(dataString2);
							long diff = date2.getTime() - date1.getTime(); // milliseconds between the first and second date
							// Convert it to byte array and store it after the bytes of the first date
							byte[] dateByte2 = longToBytes(diff);
							for (int i = 0; i < dateByte2.length; i++) {
								page[i + usedSpace + 68] = dateByte2[i];
							}
						}
					}
					
					// birthPlace label
					byte[] birthPlace = fields[2].getBytes();
					for (int i = 0; i < birthPlace.length; i++) {
						page[i + usedSpace + 76] = birthPlace[i];
					}
					
					// deathDate (same as birthDate)
					matcher = p.matcher(fields[3]);
					// If no character match the pattern, the value may be not reasonable date or NULL,
					// so just convert them to -1 as presentation, and convert -1 to byte array for storing
					if (matcher.find() == false) {
						byte[] nonDate =  longToBytes(-1L);
						for (int i = 0; i < nonDate.length; i++) {
							page[i + usedSpace + 226] = nonDate[i];
						}
					}
					else {
						// Get the format of the date
						String dFormat1 = (matcher.group(1) != null)? "yyyy-MM-dd":"yyyy/MM/dd";
						String dataString1 = (matcher.group(1) != null)? matcher.group(1):matcher.group(2);
						SimpleDateFormat ft1 = new SimpleDateFormat(dFormat1);
						Date date1 = ft1.parse(dataString1);
						byte[] dateByte1 = longToBytes(date1.getTime());
						for (int i = 0; i < dateByte1.length; i++) {
							page[i + usedSpace + 226] = dateByte1[i];
						}
						// If there are two date in the field, calculate the milliseconds between the first
						// date and the second date, and store it using long and convert it to byte array
						// in order to store the milliseconds after the bytes of the first date
						if (matcher.find() == true) {
							String dFormat2 = (matcher.group(1) != null)? "yyyy-MM-dd":"yyyy/MM/dd";
							String dataString2 = (matcher.group(1) != null)? matcher.group(1):matcher.group(2);
							SimpleDateFormat ft2 = new SimpleDateFormat(dFormat2);
							Date date2 = ft2.parse(dataString2);
							long diff = date2.getTime() - date1.getTime();
							byte[] dateByte2 = longToBytes(diff);
							for (int i = 0; i < dateByte2.length; i++) {
								page[i + usedSpace + 234] = dateByte2[i];
							}
						}
					}
					// Next five field of String type
					int[] position = {242, 442, 672, 928, 978}; // the location of the next five field in the record
					for (int i = 0; i < position.length; i++) {
						byte[] temp = fields[i + 4].getBytes();
						for (int j = 0; j < temp.length; j++) {
							page[j + usedSpace + position[i]] = temp[j];
						}
					}
					
					// wikiPageID
					// If wikiPageID is null, convert it to -1 as presentation
					if (fields[9].equals("NULL")) {
						byte[] wikiPageID = intToBytes(-1);
						for (int i = 0; i < wikiPageID.length; i++) {
							page[i + usedSpace + 1253] = wikiPageID[i];
						}
					}
					else {
						byte[] wikiPageID = intToBytes(Integer.parseInt(fields[9]));
						for (int i = 0; i < wikiPageID.length; i++) {
							page[i + usedSpace + 1253] = wikiPageID[i];
						}
					}
					
					// description
					byte[] description = fields[10].getBytes();
					for (int i = 0; i < description.length; i++) {
						page[i + usedSpace + 1257] = description[i];
					}
					// Update parameters
					usedSpace += 1592;
					recordCounter += 1;
				}
				// Write out the last page to the heap file
				os.write(page);
				pageCounter++;
				long endTime = System.currentTimeMillis();
				long usedTime = endTime - startTime;
				System.out.println("The number of records loaded: " + recordCounter);
				System.out.println("The number of pages used: " + pageCounter);
				System.out.println("The number of milliseconds to create the heap file:" + usedTime);
			}
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("command should be 'java dbload -p pagesize datafile'");
		} catch (NumberFormatException e) {
			System.out.println("please enter a number for the page size!");
		} catch (FileNotFoundException e) {
			System.out.println("please enter correct location of the data file");
		}

	}
	
	/**
	 * convert int to byte array
	 */
	public static byte[] intToBytes(int s) {
        byte[] array = new byte[4];

        for (int i = 0; i < array.length; i++) {
            array[array.length - 1 - i] = (byte) (s >> (i * 8));
        }
        return array;
    }
	
	/**
	 * convert long to byte array
	 */
	public static byte[] longToBytes(long l) {
        byte[] array = new byte[8];

        for (int i = 0; i < array.length; i++) {
            array[array.length - 1 - i] = (byte) (l >> (i * 8));
        }
        return array;
    }

}
