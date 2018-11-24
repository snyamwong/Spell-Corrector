import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Read in a file rather than a word
 * 
 * @author wongt1
 *
 */
public class SpellChecker {
	static HashMap<String, Integer> dictionary = new HashMap<>();
	static HashMap<String, Integer> map = new HashMap<String, Integer>();
	static String suggestedWord = "";

	public static void main(String[] args) throws IOException {
		// user input, creating dictionary
		Scanner scanner = new Scanner(new File("src/input.txt"));
		Scanner s = new Scanner(new File("src/american-english-JL.txt"));

		// populate the dictionary
		addToDictionary(dictionary, s);
		// spell the input file
		spellCheck(scanner);
		
		scanner.close();
	}

	/**
	 * method that cleans up the String and gets rid of symbols, etc
	 * 
	 * @param s
	 * @return
	 */
	private static String cleanUpString(String s) {
		s = s.toLowerCase();
		s = s.replaceAll("[,.-]", "");
		s = s.replaceAll("’s", "");
		s = s.replaceAll("'s", "");
		s = s.replaceAll("[â€˜™]", "");
		s = s.replaceAll("[0123456789]", "");
		s = s.replaceAll("\\s+", "");
		s = s.trim();
		return s;
	}

	/**
	 * Builds the HashMap for O(1) searching, O(n) space where n = number of
	 * words in dictionary
	 * 
	 * @param hs
	 * @param s
	 * @return
	 */
	private static HashMap<String, Integer> addToDictionary(HashMap<String, Integer> h, Scanner s) {
		int wordCounter = 0;
		while (s.hasNext()) {
			wordCounter++;
			String word = s.next();
			if (!h.containsKey(word)) {
				h.put(word, 1);
			} else {
				h.put(word, h.get(word) + 1);
			}
		}
		System.out.println(wordCounter);
		return h;
	}

	/**
	 * Checks the spelling of each word in the input.txt
	 * 
	 * @param s
	 * @throws IOException
	 */
	private static void spellCheck(Scanner s) throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/output.txt"));

		// goes through the entire file word by word
		while (s.hasNext()) {
			String word = s.next();
			// tokenize word
			word = cleanUpString(word);
			//
			if (!dictionary.containsKey(word) && word != "") {
				bufferedWriter.write(word + "\n");
				correctWord(word, bufferedWriter);
			}
		}
		bufferedWriter.close();
	}

	/**
	 * The four operations for to correct a word with an edit distance of 1
	 * 
	 * @param s
	 */
	private static void correctWord(String s, BufferedWriter bw) throws IOException {
		containsKeyALetter(s);
		subALetter(s);
		removeALetter(s);
		switchALetter(s);
		suggestedWord();
		arrayToFile(bw, map);
		map.clear();
		suggestedWord = "";
	}

	/**
	 * containsKey a letter to the string
	 * 
	 * @param s
	 * @return
	 */
	private static void containsKeyALetter(String s) {
		for (int i = 'a'; i <= 'z'; i++) {
			for (int j = 0; j <= s.length(); j++) {
				// StringBuilder to create an mutable String
				StringBuilder temp = new StringBuilder(s);
				// i == 'a', so I need to cast (char) here
				temp.insert(j, String.valueOf((char) i));
				// word is in dictionary
				if (dictionary.containsKey(temp.toString())) {
					if (!map.containsKey(temp.toString())) {
						map.put(temp.toString(), 1);
					}
					// if the suggested word has a frequency >1
					// important because else NullPointerException
					else {
						map.put(temp.toString(), map.get(temp.toString()) + 1);
					}
				}
			}
		}
	}

	/**
	 * Remove a letter to the string
	 * 
	 * @param s
	 * @return
	 */
	private static void removeALetter(String s) {
		for (int j = 0; j < s.length(); j++) {
			StringBuilder temp = new StringBuilder(s);
			temp.deleteCharAt(j);
			if (dictionary.containsKey(temp.toString())) {
				if (!map.containsKey(temp.toString())) {
					map.put(temp.toString(), 1);
				} else {
					map.put(temp.toString(), map.get(temp.toString()) + 1);
				}
			}
		}
	}

	/**
	 * Substitute a letter to the string
	 * 
	 * @param s
	 * @return
	 */
	private static void subALetter(String s) {
		for (int i = 'a'; i <= 'z'; i++) {
			for (int j = 0; j < s.length(); j++) {
				StringBuilder temp = new StringBuilder(s);
				temp.replace(j, j + 1, String.valueOf((char) i));
				if (dictionary.containsKey(temp.toString())) {
					if (!map.containsKey(temp.toString())) {
						map.put(temp.toString(), 1);
					} else {
						map.put(temp.toString(), map.get(temp.toString()) + 1);
					}
				}
			}
		}
	}

	/**
	 * Switch 2 letters to the string
	 * 
	 * @param s
	 * @return
	 */
	private static void switchALetter(String s) {
		perm("", s);
	}

	/**
	 * Done recursively
	 * 
	 * @param prefix
	 * @param s
	 */
	private static void perm(String prefix, String s) {
		int n = s.length();
		System.out.println(prefix);
		if (dictionary.containsKey(prefix) && !map.containsKey(prefix)) {
			map.put(prefix, 1);
		} else if (map.containsKey(prefix)) {
			map.put(prefix, map.get(prefix) + 1);
		} else {
			for (int i = 0; i < n; i++) {
				perm(prefix + s.charAt(i), s.substring(0, i) + s.substring(i + 1, n));
			}
		}
	}

	/**
	 * Suggested Word - based on frequency
	 */
	private static void suggestedWord() {
		int max = Integer.MIN_VALUE;
		for (String s : map.keySet()) {
			System.out.println(s + " " + map.get(s));
			// omits one letter words
			if (max < map.get(s) && s.length() > 1) {
				max = map.get(s);
				suggestedWord = s;
			}
		}
		System.out.println("---");
	}

	/**
	 * Read in from arrayList into the file
	 * 
	 * @param bw
	 * @throws IOException
	 */
	public static void arrayToFile(BufferedWriter bw, HashMap<String, Integer> map) throws IOException {
		bw.write("List of possible correct words: ");
		for (String item : map.keySet()) {
			bw.write(item + " ");
		}
		bw.write("\nSuggested word: " + suggestedWord);
		bw.newLine();
	}
}
