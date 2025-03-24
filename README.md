# Autocorrect
#### A word-suggestion project created by Zach Blick for Adventures in Algorithms at Menlo School in Atherton, CA

This program takes in a (potentially misspelled) word from the user and, if it is misspelled, suggests other words that user might have been intending to type. It does so by generating a list of candidate words, then evaluating these candidates to see how similar they are to the typed word.

Candidates are generated using one of two different algorithms depending on the typed word's length. If the word is short (up to 3 letters), the candidates are all short words. If the word is longer, the candidates are all words that share a substring of length 3 (called a 3-gram) with the typed word. This is done using a hash map; when the program begins, the provided dictionary is parsed to allow quick lookups of all the words containing a given 3-gram. While this is being done, a list of all short words is made to allow candidates to be known instantly in that case.

After generating this list of candidates, each candidate is evaluated using a stronger metric. Again, the algorithms differ slightly for short and long words. Longer words are evaluated using the edit distance (the number of "edits" required to transform one word into another, where an "edit" is defined as the addition, removal, or in-place substitution of a single letter). For shorter words, the algorithm is similar but slightly different; the distance is calculated in the same way, but the only allowable "edit" is in-place substitutions.

Finally, up to 8 words with the lowest distances are printed to the terminal, and the process repeats.