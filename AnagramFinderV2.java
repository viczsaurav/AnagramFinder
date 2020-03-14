import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This java program lets you find all Anagrams of an input string present in the dictionary.
 *
 * The assumption here is:
 *  - Distributed
 *  - High usage
 *  - Multi-word lines in dictionary must be skipped
 *
 *
 * Due to above assumptions, in the current approach:
 *  - We save all dictionary words in a HashMap with Key => Hash(string) and Value => List<String>
 *  - Return and print the found anagrams.
 */

public class AnagramFinderV2 {

	private static final Map<String, List<String>> dictionary =  new ConcurrentHashMap<>();
	private static final String EXIT_STR = "EXIT";

	public static void main(String[] args) throws Exception{

		System.out.println("\nWelcome to the Anagram Finder V2\n--------------------------------");

		// Populate the dictionary Set
		readDictionary(args);

		// User input
		try (Scanner scanner = new Scanner(System.in)) {
			String input;  // Read user input
			while(true){
				System.out.print("\nAnagramFinderV2> ");
				input = scanner.nextLine();  // Read user input

				if(input.length()==0 || input.split(" ").length>1){
					System.out.println("Enter valid input, skipping...");
					continue;
				}
				if (!input.equalsIgnoreCase(EXIT_STR)){
					processInput(input);
				} else {
					break;
				}
			}
		}
		System.out.println("Exiting..\n");
	}

	/**
	 * Read dictionary and populate the set
	 * @param args
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	private static void readDictionary(String[] args) throws IOException {
		if(args.length<1){
			throw new IllegalArgumentException("Please provide the input dictionary..");
		}
		Path path = Paths.get(args[0]);
		if(path==null) {
			throw new FileNotFoundException("Cannot find file in current folder: "+ args[0]);
		}
		long startTime = System.currentTimeMillis();
		try(Stream<String> lines = Files.lines(path)){
            lines.parallel()
                 .map(String::toLowerCase)
				 .forEach(word -> {
                    if(word.split(" ").length>1){
                        System.out.println("Multi-word not supported: ["+ word + "], skipping...");
                    } else {
                        dictionary.compute(sortString(word),
                                        (k,v) -> (v==null)? new ArrayList<>():v)
                                        .add(word);
                    }
                });
        }
		catch(IOException e){
			System.out.println("Error while processing dictionary file: "+ e.getMessage());
			throw e;
		}
        displayExecutionTime("Dictionary Loaded", startTime);
        // System.out.println(dictionary.size());
        // dictionary.entrySet().forEach(e -> System.out.println(e.getKey()+"->"+e.getValue()));
	}

	private static String sortString(String input){
		char[] chars = input.toCharArray();
		Arrays.sort(chars);
		return new String(chars);
	}

	/**
	 * Process each user input string
	 * @param word
	 */
	private static void processInput(String word) {
		long startTime = System.currentTimeMillis();
		List<String> foundAnagrams = fetchAnagramsFromDictionary(word);
		displayExecutionTime(foundAnagrams.size() + " Anagrams found for " + word, startTime);
		System.out.println(String.join(",", foundAnagrams));
	}

	/**
	 * fetch all found anagrams from the dictionary
	 * @param word
	 * @return
	 */
	private static List<String> fetchAnagramsFromDictionary(String word){
		String sortedword = sortString(word);
		List<String> foundWords = dictionary.get(sortedword);
		return foundWords!=null? foundWords: new ArrayList<>();
	}

	/**
	 * Display execution time of the activity in Milli seconds
	 * @param activity
	 * @param startTime
	 */
	private static void displayExecutionTime(String activity, long startTime) {
		long finishTime = System.currentTimeMillis();
		double elapsedTime = (finishTime - startTime);
		System.out.println(activity +" in " + elapsedTime + " ms");
	}
}