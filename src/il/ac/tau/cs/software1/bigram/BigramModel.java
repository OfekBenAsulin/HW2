package il.ac.tau.cs.software1.bigram;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Locale;

public class BigramModel {
    public static final int MAX_VOCABULARY_SIZE = 14500;
    public static final String VOC_FILE_SUFFIX = ".voc";
    public static final String COUNTS_FILE_SUFFIX = ".counts";
    public static final String SOME_NUM = "some_num";
    public static final int ELEMENT_NOT_FOUND = -1;

    String[] mVocabulary;
    int[][] mBigramCounts;

    // DO NOT CHANGE THIS !!!
    public void initModel(String fileName) throws IOException {
        mVocabulary = buildVocabularyIndex(fileName);
        mBigramCounts = buildCountsArray(fileName, mVocabulary);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods - already implemented for you. Use them!
    ///////////////////////////////////////////////////////////////////////////

    /*
     * Converts a single token (a whitespace-free piece of text from the file)
     * into the form in which it is stored in the vocabulary:
     *   - a token with at least one English letter  -> the token in lowercase
     *   - a token made only of digits (an integer)  -> SOME_NUM
     *   - anything else is not a legal word         -> null
     *
     * @pre: token != null
     */
    public static String normalizeWord(String token) {
        boolean hasLetter = false;
        boolean allDigits = !token.isEmpty();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                hasLetter = true;
            }
            if (c < '0' || c > '9') {
                allDigits = false;
            }
        }
        if (hasLetter) {
            return token.toLowerCase(Locale.ROOT);
        }
        if (allDigits) {
            return SOME_NUM;
        }
        return null;
    }

    /*
     * @pre: vocabulary != null
     * @post: $ret = the index of word in vocabulary, or ELEMENT_NOT_FOUND if
     * word is null or does not appear in it
     */
    public static int findIndex(String[] vocabulary, String word) {
        if (word == null) {
            return ELEMENT_NOT_FOUND;
        }
        for (int i = 0; i < vocabulary.length; i++) {
            if (word.equals(vocabulary[i])) {
                return i;
            }
        }
        return ELEMENT_NOT_FOUND;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Questions 1 - 5: implement these
    ///////////////////////////////////////////////////////////////////////////

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public String[] buildVocabularyIndex(String fileName) throws IOException { // Question 1
        String[] vocabulary = new String[MAX_VOCABULARY_SIZE];
        File fromFile = new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(fromFile));
        String line;
        String[] tokens;
        int lastIndex = 0;
        while ((line = reader.readLine()) != null) { // Read each line from the file
            tokens = line.trim().split("\\s+");
            for (String token : tokens) { // for each token in the line, normalize it and add it to the vocabulary if it's not already there
                String normalized = normalizeWord(token);
                if (findIndex(vocabulary, normalized) == ELEMENT_NOT_FOUND && normalized != null) {
                    vocabulary[lastIndex++] = normalized;
                    if (lastIndex >= MAX_VOCABULARY_SIZE) { // If the vocabulary is full, close the reader and return the vocabulary
                        reader.close();
                        return vocabulary;
                    }
                }
            }
        }
        reader.close();
        if (lastIndex > 0 && lastIndex < MAX_VOCABULARY_SIZE) { //If the vocabulary is not full, return a copy of the vocabulary with the correct size
            String[] result = new String[lastIndex];
            System.arraycopy(vocabulary, 0, result, 0, lastIndex);
            return result;
        }
        else{
            if (lastIndex == 0) {
                return new String[0]; // Return an empty array if no words were found
            }
        }
        return null;
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public int[][] buildCountsArray(String fileName, String[] vocabulary) throws IOException { // Question 2
        int[][] bigramCounts = new int[vocabulary.length][vocabulary.length];
        File fromFile = new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(fromFile));
        String line;
        String[] tokens;
        while ((line = reader.readLine()) != null) { // Read each line from the file
            tokens = line.trim().split("\\s+");
            for (int i = 0; i < tokens.length - 1; i++) { // for each token in the line, normalize it and update the bigram counts
                String normalized1 = normalizeWord(tokens[i]);
                String normalized2 = normalizeWord(tokens[i + 1]);
                int index1 = findIndex(vocabulary, normalized1);
                int index2 = findIndex(vocabulary, normalized2);
                if (index1 != ELEMENT_NOT_FOUND && index2 != ELEMENT_NOT_FOUND) {
                    bigramCounts[index1][index2]++;
                }
            }
        }
        reader.close();
        return bigramCounts;
    }

    /*
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: fileName is a legal file path
     */
    public void saveModel(String fileName) throws IOException { // Question 3
        File vocFile = new File(fileName+VOC_FILE_SUFFIX);
        File countsFile = new File(fileName+COUNTS_FILE_SUFFIX);
        BufferedWriter vocWriter = new BufferedWriter(new FileWriter(vocFile));
        BufferedWriter countsWriter = new BufferedWriter(new FileWriter(countsFile));
        String line;
        vocWriter.write(mVocabulary.length + " words");
        vocWriter.newLine();
        for (int i = 0; i < mVocabulary.length; i++) {
            line =String.format("%d,%s",i,mVocabulary[i] ); // Write the vocabulary to the file
            vocWriter.write(line);
            vocWriter.newLine();
        }
        vocWriter.close();
        for(int i = 0; i < mBigramCounts.length; i++) {
            for (int j = 0; j < mBigramCounts[i].length; j++) {
                if (mBigramCounts[i][j] > 0) { // Write the bigram counts to the file
                    line = String.format("%d,%d:%d",i,j,mBigramCounts[i][j]);
                    countsWriter.write(line);
                    countsWriter.newLine();
                }
            }
        }
        countsWriter.close();
    }

    /*
     * @pre: fileName is a legal file path
     */
    public void loadModel(String fileName) throws IOException { // Question 4
        File vocFile = new File(fileName+VOC_FILE_SUFFIX);
        File countsFile = new File(fileName+COUNTS_FILE_SUFFIX);
        BufferedReader vocReader = new BufferedReader(new FileReader(vocFile));
        BufferedReader countsReader = new BufferedReader(new FileReader(countsFile));
        String line = vocReader.readLine();
        int vocSize = Integer.parseInt(line.split("\\s+")[0]); // Read the first line of the vocabulary file
        int[][] bigramCounts = new int[vocSize][vocSize];
        String[] vocabulary = new String[vocSize];
        while ((line = vocReader.readLine()) != null) { // Read the vocabulary from the file
            String[] tokens = line.split(",");
            int index = Integer.parseInt(tokens[0]);
            String word = tokens[1];
            vocabulary[index] = word;
        }
        vocReader.close();
        while ((line = countsReader.readLine()) != null) {// Read the bigram
            String[] tokens = line.split("[,:]");
            int index1 = Integer.parseInt(tokens[0]);
            int index2 = Integer.parseInt(tokens[1]);
            int count = Integer.parseInt(tokens[2]);
            bigramCounts[index1][index2] = count;
        }
        countsReader.close();
        this.mVocabulary = vocabulary;
        this.mBigramCounts = bigramCounts;
    }

    /*
     * @pre: sentence is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: each two words in the sentence are separated with a single space
     * @post: if sentence is probable, according to the model, $ret = true, else, $ret = false
     */
    public boolean isLegalSentence(String sentence) { // Question 5
        String[] tokens = sentence.trim().split("\\s+");
        if(tokens[0].isEmpty()) {
            return true;
        }
        else if (tokens.length == 1) {
            return getWordIndex(tokens[0]) != ELEMENT_NOT_FOUND;
        }
        for (int i = 0; i < tokens.length - 1; i++) {
            String word1 = tokens[i];
            String word2 = tokens[i + 1];
            int index1 = getWordIndex(word1);
            int index2 = getWordIndex(word2);
            if (index1 == ELEMENT_NOT_FOUND || index2 == ELEMENT_NOT_FOUND) {
                return false;
            }
            if (mBigramCounts[index1][index2] == 0) {
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Queries on the model - already implemented for you.
    // They start working once Questions 1 - 2 are done.
    ///////////////////////////////////////////////////////////////////////////

    /*
     * @pre: word is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = -1 if word is not in vocabulary, otherwise $ret = the index of word in vocabulary
     */
    public int getWordIndex(String word) {
        return findIndex(mVocabulary, word);
    }

    /*
     * @pre: word1, word2 are in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = the count for the bigram <word1, word2>. if one of the words does not
     * exist in the vocabulary, $ret = 0
     */
    public int getBigramCount(String word1, String word2) {
        int index1 = getWordIndex(word1);
        int index2 = getWordIndex(word2);
        if (index1 == ELEMENT_NOT_FOUND || index2 == ELEMENT_NOT_FOUND) {
            return 0;
        }
        return mBigramCounts[index1][index2];
    }

    /*
     * @pre: word is in lowercase, and is in mVocabulary
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = the word with the lowest vocabulary index that appears most frequently after word
     * (if a bigram starting with word was never seen, $ret will be null)
     */
    public String getMostFrequentProceeding(String word) {
        int[] row = mBigramCounts[getWordIndex(word)];
        int best = ELEMENT_NOT_FOUND;
        for (int i = 0; i < row.length; i++) {
            if (row[i] > 0 && (best == ELEMENT_NOT_FOUND || row[i] > row[best])) {
                best = i;
            }
        }
        return best == ELEMENT_NOT_FOUND ? null : mVocabulary[best];
    }

    /*
     * @pre: arr1.length = arr2.length
     * @post: if arr1 or arr2 are only filled with zeros, $ret = -1, otherwise calculates CosineSim
     */
    public static double calcCosineSim(int[] arr1, int[] arr2) {
        double dot = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < arr1.length; i++) {
            dot += (double) arr1[i] * arr2[i];
            norm1 += (double) arr1[i] * arr1[i];
            norm2 += (double) arr2[i] * arr2[i];
        }
        if (norm1 == 0 || norm2 == 0) {
            return -1;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /*
     * @pre: word is in vocabulary
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = w implies that w is the word with the largest cosineSimilarity(vector for word, vector for w)
     * among all the other words in vocabulary (the lowest index wins a tie)
     */
    public String getClosestWord(String word) {
        int wordIndex = getWordIndex(word);
        int best = ELEMENT_NOT_FOUND;
        double bestSim = 0;
        for (int i = 0; i < mVocabulary.length; i++) {
            if (i == wordIndex) {
                continue;
            }
            double sim = calcCosineSim(mBigramCounts[wordIndex], mBigramCounts[i]);
            if (best == ELEMENT_NOT_FOUND || sim > bestSim) {
                best = i;
                bestSim = sim;
            }
        }
        // A vocabulary with a single word: the closest word is the word itself
        return best == ELEMENT_NOT_FOUND ? word : mVocabulary[best];
    }
}
