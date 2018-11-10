package MorphologicalDisambiguation;

import DataStructure.CounterHashMap;
import MorphologicalAnalysis.FsmParseList;

import java.io.*;
import java.util.HashMap;

public class RootWordStatistics implements Serializable {
    private HashMap<String, CounterHashMap<String>> statistics;

    /**
     * Constructor of {@link RootWordStatistics} class that generates a new {@link HashMap} for statistics.
     */
    public RootWordStatistics() {
        statistics = new HashMap<String, CounterHashMap<String>>();
    }

    /**
     * Constructor of {@link RootWordStatistics} class which fetches the statistics from given input file.
     *
     * @param fileName File to get statistics.
     */
    public RootWordStatistics(String fileName) {
        ObjectInputStream inObject;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            inObject = new ObjectInputStream(classLoader.getResourceAsStream(fileName));
            statistics = (HashMap<String, CounterHashMap<String>>) inObject.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor of {@link RootWordStatistics} class which fetches the statistics from given input file.
     *
     * @param inputStream File to get statistics.
     */
    public RootWordStatistics(FileInputStream inputStream){
        ObjectInputStream inObject;
        try {
            inObject = new ObjectInputStream(inputStream);
            statistics = (HashMap<String, CounterHashMap<String>>) inObject.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Method to check whether statistics contains the given String.
     *
     * @param key String to look for.
     * @return Returns <tt>true</tt> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(String key) {
        return statistics.containsKey(key);
    }

    /**
     * Method to get the value of the given String.
     *
     * @param key String to look for.
     * @return Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
     */
    public CounterHashMap<String> get(String key) {
        return statistics.get(key);
    }

    /**
     * Method to associates a String along with a {@link CounterHashMap} in the statistics.
     *
     * @param key            Key with which the specified value is to be associated.
     * @param wordStatistics Value to be associated with the specified key.
     */
    public void put(String key, CounterHashMap<String> wordStatistics) {
        statistics.put(key, wordStatistics);
    }

    /**
     * Method to save statistics into file specified with the input file name.
     *
     * @param fileName File to save the statistics.
     */
    public void saveStatistics(String fileName) {
        FileOutputStream outFile;
        ObjectOutputStream outObject;
        try {
            outFile = new FileOutputStream(fileName);
            outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(statistics);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The bestRootWord method gets the root word of given {@link FsmParseList} and if statistics has a value for that word,
     * it returns the max value associated with that word.
     *
     * @param parseList {@link FsmParseList} to check.
     * @param threshold A double value for limit.
     * @return The max value for the root word.
     */
    public String bestRootWord(FsmParseList parseList, double threshold) {
        String rootWords = parseList.rootWords();
        if (statistics.containsKey(rootWords)) {
            CounterHashMap<String> rootWordStatistics = statistics.get(rootWords);
            return rootWordStatistics.max(threshold);
        }
        return null;
    }

}
