import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This java program lets you find all Anagrams of an input string present in the dictionary.
 *
 * The assumption here is:
 *  - High usage
 *  - Multi-word lines in dictionary must be skipped
 *  - No special characters, digits and spaces allowed
 *
 *
 * Due to above assumptions, in the current approach:
 *  - We save all dictionary words in a HashMap with Key => Hash(string) and Value => List<String>
 *  - Return and print the found anagrams.
 *
 * ------------------*
 * Time Complexity: -*
* -------------------*
 * Dictionary Population : O(N*M) N = number of words in dictionary, M is max length word
 *  forEach word:(N Words)
 *   -- calculate identifier (O(M))
 *   -- Get Value List from Map if exists(O(1))
 *   -- Add new Value in List(O(1))
 *
 * Search: O(Max(M,N)) N = number of words in dictionary, M is max length word
 *   -- Get identifier(O(M))
 *   -- Retrieve list from Map(O(1))
 *   -- Print String.join(",", list) (O(K)) where k= number of words, max K=N
 */

public class AnagramFinderV2 {

	private static final String EXIT_STR = "EXIT";

	private static final Map<Long, List<String>> dictionary =  new ConcurrentHashMap<>();
	private static Pattern validStringPattern;

	// Constructor
	public AnagramFinderV2(String fileName) throws Exception{
		validStringPattern = Pattern.compile("^[a-zAA-Z]*");
		readDictionary(fileName, validStringPattern);
	}

	public static void main(String[] args) throws Exception{
		System.out.println("\nWelcome to the Anagram Finder V2\n--------------------------------");
		if(args.length<1){
			throw new IllegalArgumentException("Please provide the input dictionary..");
		}
		AnagramFinderV2 finder = new AnagramFinderV2(args[0]);

		// User input
		try (Scanner scanner = new Scanner(System.in)) {
			String input;  // Read user input
			while(true){
				System.out.print("\nAnagramFinderV2> ");
				input = scanner.nextLine();  // Read user input

				if(!isValidString(input, validStringPattern)){
					System.out.println("Enter valid input, skipping...");
				} else if (!input.equalsIgnoreCase(EXIT_STR)){
					finder.findAnagrams(input);
				} else {
					break;
				}
			}
		}
		System.out.println("Exiting..\n");
	}

	/**
	 * Read dictionary and populate the set
	 * Time complexity: O(MN) where N = number of words, M = length of words
	 * Space Complexity: O(N+M)
	 * @param fileName
	 * @throws IOException
	 */
	private static void readDictionary(String fileName, Pattern validStringPattern) throws IOException {
		long startTime = System.currentTimeMillis();

		Path path = Paths.get(fileName);
		if(path==null) {
			throw new FileNotFoundException("Cannot find file in current folder: "+ fileName);
		}

		// Processing dictionary
		try(Stream<String> lines = Files.lines(path)){
			lines.parallel()
					.filter(word -> isValidString(word, validStringPattern))
					.map(String::toLowerCase)
					.forEach(word -> dictionary.compute(getStringIdentifier(word),
									(k,v) -> (v==null)? new ArrayList<>():v)
									.add(word));
		}
		catch(IOException e){
			System.out.println("Error while processing dictionary file: "+ e.getMessage());
			throw e;
		}
		displayExecutionTime("Dictionary Loaded", startTime);
	}

	/**
	 * Process each user input string
	 * @param word
	 */
	private void findAnagrams(String word) {
		long startTime = System.currentTimeMillis();
		List<String> foundAnagrams = this.fetchAnagramsFromDictionary(word);
		displayExecutionTime(foundAnagrams.size() + " Anagrams found for " + word, startTime);
		System.out.println(String.join(",", foundAnagrams));
	}

	/**
	 * fetch all found anagrams from the dictionary
	 * Time complexity: O(M)
	 * 
	 * @param word String
	 * @return boolean
	 */
	private List<String> fetchAnagramsFromDictionary(String word){
		long identifier = getStringIdentifier(word);
		List<String> foundWords = dictionary.get(identifier);
		return foundWords!=null? foundWords: new ArrayList<>();
	}

	/**
	 * Validating String : No space, special characters and digit
	 * @param word
	 * @param validStringPattern
	 * @return
	 */
	private static boolean isValidString(String word, Pattern validStringPattern){
		Matcher matcher = validStringPattern.matcher(word);
		return (word.length()>0 && matcher.matches());
    }
    
    /**
     * Converts each string to long indetifier O(M)
     * @param word
     * @return
     */
	private static long getStringIdentifier(String word){
		int[] primeArray = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31,
						37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
						107, 109, 113 };
        
    // Lowercase a has ASCII value 97 in decimal .So for z ,the value is 122 in decimal.
		long product=1;
		for(int ch: word.toCharArray()){
			product *= primeArray[ch-'a'];
		}
		return product;
	}

	/**
	 * Display execution time of the activity in Milli seconds
	 * @param activity
	 * @param startTime
	 */
	private static void displayExecutionTime(String activity, long startTime) {
		long finishTime = System.currentTimeMillis();
		System.out.println(activity +" in " + (finishTime - startTime) + " ms");
	}
}