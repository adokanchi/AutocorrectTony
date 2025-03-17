import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Tony Dokanchi
 */
public class Autocorrect {
    private ArrayList<ArrayList<String>> dict;
    private static int threshold;
    private static final int R = 28;
    private static final int n = 4;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */

    public Autocorrect(String[] words, int threshold) {
        dict = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < Math.pow(R, n); i++) {
            dict.add(new ArrayList<String>());
        }
        for (String word : words) {
            ArrayList<Integer> hashes = getHashes(word);
            for (int hash : hashes) {
                dict.get(hash).add(word);
            }
        }
        Autocorrect.threshold = threshold;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        // Generate candidates based on n-grams
        ArrayList<String> candidates = new ArrayList<String>();
        ArrayList<Integer> hashes = getHashes(typed);
        for (int hash : hashes) {
            for (String candidate : dict.get(hash)) {
                candidates.add(candidate);
            }
        }

        // Evaluate candidates based on edit distance
        ArrayList<String> correctWords = new ArrayList<String>();
        for (String word : candidates) {
            if (ed(word, typed) < threshold) {
                correctWords.add(word);
            }
        }

        // Remove duplicates
        correctWords = removeDuplicates(correctWords);

        // Sort in alphabetical order
        Collections.sort(correctWords);

        String[] correct = new String[correctWords.size()];
        correctWords.toArray(correct);
        return correct;
    }

    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int ed(String s1, String s2) {
        int n1 = s1.length();
        int n2 = s2.length();

        // dp(i,j) = include i letters of s1 and j letters of s2
        int[][] dp = new int[n1 + 1][n2 + 1];

        for (int i = 0; i <= n1; i++) {
            for (int j = 0; j <= n2; j++) {
                // Empty string case
                if (i == 0 || j == 0) {
                    dp[i][j] = Math.max(i, j);
                }
                // Matching final character case
                else if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                }
                // General case
                else {
                    dp[i][j] = 1 + min(ed(tail(s1), tail(s2)), ed(s1, tail(s2)), ed(tail(s1), s2));
                }
            }
        }
        return dp[n1][n2];
    }

    // Removes the final character, assuming s is not empty string
    private static String tail(String s) {
        return s.substring(0, s.length() - 1);
    }

    // Min of three integers
    private static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    private static ArrayList<Integer> getHashes(String word) {
        int len = word.length();
        if (len < n) return new ArrayList<Integer>();

        ArrayList<Integer> hashes = new ArrayList<Integer>();
        int hash = 0;
        for (int i = 0; i < n - 1; i++) {
            hash *= R;
            hash += getVal(word.charAt(i));
        }
        for (int i = n - 1; i < len; i++) {
            // i is the index of the last letter (the letter being added)
            hash *= R;
            hash += getVal(word.charAt(i));
            hash %= Math.pow(R, n);

            hashes.add(hash);
        }
        return hashes;
    }

    private static int getVal(char c) {
        if (c == '-') return 26;
        if (c == '\'') return 27;
        return c - 'a';
    }

    private static ArrayList<String> removeDuplicates(ArrayList<String> list) {
        int p = 1_618_033_989;
        int R = 256;
        ArrayList<String> result = new ArrayList<String>();
        boolean[] seen = new boolean[p];
        for (String s : list) {
            boolean inNewList = false;
            // If the hash matches a previously seen hash
            if (seen[hash(s, R, p)]) {
                // Check if the string matches a previous string
                for (String word : result) {
                    if (word.equals(s)) {
                        inNewList = true;
                        break;
                    }
                }
            }
            if (!inNewList) {
                result.add(s);
                seen[hash(s, R, p)] = true;
            }
        }
        return result;
    }

    private static int hash(String s, int R, int p) {
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            result *= R;
            result %= p;
            result += s.charAt(i);
            result %= p;
        }
        return result;
    }
}