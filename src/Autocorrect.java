import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

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
    private ArrayList<String> shortWords;
    private int threshold;
    private static final int R = 27;
    private static final int n = 2;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */

    public Autocorrect(String[] words, int threshold) {
        dict = new ArrayList<>();
        shortWords = new ArrayList<>();
        for (int i = 0; i < Math.pow(R, n); i++) {
            dict.add(new ArrayList<>());
        }
        for (String word : words) {
            ArrayList<Integer> hashes = getHashes(word);
            for (int hash : hashes) {
                dict.get(hash).add(word);
            }
            if (word.length() <= 5) {
                shortWords.add(word);
            }
        }
        this.threshold = threshold;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        ArrayList<String> candidates = new ArrayList<>();

        if (typed.length() <= 3) {
            candidates = shortWords;
            threshold = 1;
        }
        else {
            // Generate candidates based on n-grams
            ArrayList<Integer> hashes = getHashes(typed);
            for (int hash : hashes) {
                candidates.addAll(dict.get(hash));
            }

            // Remove duplicates
            candidates = removeDuplicates(candidates);
        }

        // Evaluate candidates based on edit distance
//        threshold = Math.max(1, typed.length() / 3);
        ArrayList<ArrayList<String>> correctWords = new ArrayList<>();
        for (int i = 0; i <= threshold; i++) {
            correctWords.add(new ArrayList<>());
        }
        for (String word : candidates) {
            int ed = ed(word, typed);
            if (ed <= threshold) {
                correctWords.get(ed).add(word);
            }
        }

        // Sort in alphabetical order
        for (ArrayList<String> list : correctWords) {
            Collections.sort(list);
        }

        ArrayList<String> words = new ArrayList<>();

        for (ArrayList<String> list : correctWords) {
            words.addAll(list);
        }

        return words.toArray(new String[0]);
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

        // Empty string case
        for (int i = 1; i <= n1; i++) {
            dp[i][0] = i;
        }
        for (int j = 1; j <= n2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= n1; i++) {
            for (int j = 1; j <= n2; j++) {
                // Matching final character case
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                }
                // General case
                else {
                    dp[i][j] = 1 + min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]);
                }
            }
        }
        return dp[n1][n2];
    }

    // Min of three integers
    private static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    private static ArrayList<Integer> getHashes(String word) {
        int len = word.length();
        ArrayList<Integer> hashes = new ArrayList<>();
        int modulus = (int) Math.pow(R, n);
        int hash = 0;
        for (int i = 0; i < n - 1; i++) {
            hash *= R;
            hash += getVal(word.charAt(i));
        }
        for (int i = n - 1; i < len; i++) {
            // i is the index of the last letter (the letter being added)
            hash *= R;
            hash += getVal(word.charAt(i));
            hash %= modulus;

            hashes.add(hash);
        }
        return hashes;
    }

    private static int getVal(char c) {
        if (c == '\'') return 26;
        return c - 'a';
    }

    private static ArrayList<String> removeDuplicates(ArrayList<String> list) {
        int p = 999_991;
        int R = 256;
        ArrayList<String> result = new ArrayList<>();
        boolean[] seen = new boolean[p];
        for (String s : list) {
            boolean inNewList = false;
            // If the hash matches a previously seen hash
            if (seen[hash(s)]) {
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
                seen[hash(s)] = true;
            }
        }
        return result;
    }

    // Uses a polynomial rolling hash function with radix R and size p to hash the string s
    private static int hash(String s) {
        int p = 999_991;
        int R = 256;
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            result *= R;
            result %= p;
            result += s.charAt(i);
            result %= p;
        }
        return result;
    }

    public static void main(String[] args) {
        Autocorrect a = new Autocorrect(loadDictionary("large"), 2);
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter word: ");
        String input = sc.nextLine();
        String[] corrections;
        while (!input.equals("!")) {
            corrections = a.runTest(input);
            for (String word : corrections) {
                System.out.println(word);
            }
            System.out.println("Enter word: ");
            input = sc.nextLine();
        }
    }
}