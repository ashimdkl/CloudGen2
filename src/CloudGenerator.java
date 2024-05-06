import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Program to generate a "tag cloud" from a text file, showing the most frequent
 * words in varying sizes.
 *
 * @author Szcheng Chen, Ashim Dhakal
 */
public final class CloudGenerator {

    /**
     * string containing characters considered as word separators. This includes
     * whitespace, punctuation, and special characters typically not part of a
     * word.
     */
    private static final String SEPARATORS = " \t, \n\r,.<>/?;:\"'{}[]_-+=~`!@#$%^&*()|";

    /**
     * maximum font size to be used in the tag cloud. Represents the font size
     * for the most frequently occurring word.
     */
    private static final int FMAX = 48;

    /**
     * minimum font size to be used in the tag cloud. Represents the font size
     * for the least frequently occurring words that are still included in the
     * cloud.
     */
    private static final int FMIN = 11;;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private CloudGenerator() {
    }

    /**
     * processes the input text to count the frequency of each word.
     *
     * @param input
     *            the BufferedReader object for reading the input text.
     * @return A Map from word strings to their frequencies.
     */
    private static Map<String, Integer> processText(BufferedReader input)
            throws IOException {
        Map<String, Integer> wordsCount = new HashMap<>();

        String line;
        while ((line = input.readLine()) != null) {
            int start = -1;
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if (isSeparator(ch)) {
                    if (start != -1) {
                        String word = line.substring(start, i).toLowerCase();
                        wordsCount.put(word,
                                wordsCount.getOrDefault(word, 0) + 1);
                        start = -1;
                    }
                } else if (start == -1) {
                    start = i;
                }
            }
            if (start != -1) { // Handle word at the end of the line
                String word = line.substring(start).toLowerCase();
                wordsCount.put(word, wordsCount.getOrDefault(word, 0) + 1);
            }
        }
        return wordsCount;
    }

    /**
     * Reports if given character is separator.
     *
     * @param ch
     *            the character to be checked
     * @return true if {@code ch} is separator
     */
    private static boolean isSeparator(char ch) {
        return SEPARATORS.indexOf(ch) != -1;
    }

    /**
     * Comparator class for sorting map entries by their value in descending
     * order.
     */
    private static class IntegerDescendingComparator
            implements Comparator<Map.Entry<String, Integer>>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * Comparator class for sorting map entries by their key alphabetically.
     */
    private static class StringAlphabeticalComparator
            implements Comparator<Map.Entry<String, Integer>>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    /**
     * Sorts the occurrences map by their values (frequencies) in descending
     * order.
     *
     * @param occurrences
     *            the map containing word occurrences to sort.
     * @return A list sorted by word count in descending order.
     */
    private static List<Map.Entry<String, Integer>> numSorting(
            Map<String, Integer> occurrences) {
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(
                occurrences.entrySet());

        Collections.sort(sortedList, new IntegerDescendingComparator());

        return sortedList;
    }

    /**
     * Converts a list sorted by numerical order into one sorted alphabetically.
     *
     * @param sortedByCount
     *            the list sorted by numerical order.
     * @param number
     *            the number of terms to be shown in the final result.
     * @return A list sorted alphabetically.
     */
    private static List<Map.Entry<String, Integer>> alphaSorting(
            List<Map.Entry<String, Integer>> sortedByCount, int number) {

        List<Map.Entry<String, Integer>> sortedList = sortedByCount.subList(0,
                Math.min(number, sortedByCount.size()));

        Collections.sort(sortedList, new StringAlphabeticalComparator());

        return sortedList;
    }

    /**
     * Generates the output HTML file displaying the word cloud.
     *
     * @param sortedTerms
     *            the List containing sorted terms to display.
     * @param number
     *            the number of words to include in the output.
     * @param out
     *            the PrintWriter for outputting the HTML file.
     * @param inputFile
     *            the name of the input file to reference in the output.
     */
    private static void generateOutput(PrintWriter out,
            List<Entry<String, Integer>> sortedTerms, int number,
            String inputFile) {

        out.print("<html>\r\n" + "<head>\r\n" + "<title>Top " + number
                + " words in " + inputFile + "</title>\r\n"
                + "<link href=\"http://web.cse.ohio-state.edu/software/2231/"
                + "web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\""
                + " rel=\"stylesheet\" type=\"text/css\">\r\n"
                + "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">\r\n"
                + "</head>\r\n" + "<body>\r\n" + "<h2>Top " + number
                + " words in " + inputFile + "</h2>\r\n" + "<hr>\r\n"
                + "<div class=\"cdiv\">\r\n" + "<p class=\"cbox\">");

        // Calculate font size scaling factors
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (Entry<String, Integer> term : sortedTerms) {
            int count = term.getValue();
            if (count > max) {
                max = count;
            }
            if (count < min) {
                min = count;
            }
        }
        double scale = (double) (FMAX - FMIN) / (max - min);

        // Generate tag cloud content
        for (Entry<String, Integer> entry : sortedTerms) {
            int fontSize = (int) ((entry.getValue() - min) * scale) + FMIN;
            out.println("<span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count: " + entry.getValue() + "\">"
                    + entry.getKey() + "</span> ");
        }

        // Close HTML document
        out.println("</p>\r\n" + "</div>\r\n" + "</body>\r\n" + "</html>");
    }

    /**
     * Main method to run the cloud generator program.
     *
     * @param args
     *            command line arguments (unused).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the name of the input file: ");
        String inputFileName = scanner.nextLine();
        System.out.print("Please enter the name of the output file: ");
        String outputFileName = scanner.nextLine();
        System.out.print(
                "Enter the number of words to include in the tag cloud: ");
        int number = scanner.nextInt();
        scanner.close();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(inputFileName));
                PrintWriter writer = new PrintWriter(
                        new BufferedWriter(new FileWriter(outputFileName)))) {
            //process the input text and count word frequencies
            Map<String, Integer> wordsCount = processText(reader);

            //sort the words by frequency in descending order
            List<Map.Entry<String, Integer>> sortedWords = numSorting(
                    wordsCount);

            if (sortedWords.size() < number) {
                System.out.println(
                        "The input number is out of bounds! There are only "
                                + sortedWords.size()
                                + " unique words in the file.");
            } else if (number < 0) {
                System.out.println("Invalid number.");
            } else {
                // sort the required number of words alphabetically
                List<Map.Entry<String, Integer>> finalSortedWords = alphaSorting(
                        sortedWords, number);

                // generate the tag cloud and write it to the output file
                generateOutput(writer, finalSortedWords, number, inputFileName);
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

}
