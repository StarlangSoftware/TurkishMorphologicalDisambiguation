package MorphologicalDisambiguation;

import DataStructure.CounterHashMap;
import MorphologicalAnalysis.FsmParseList;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        ClassLoader classLoader = getClass().getClassLoader();
        readFromFile(classLoader.getResourceAsStream(fileName));
    }

    /**
     * Constructor of {@link RootWordStatistics} class which fetches the statistics from given input file.
     *
     * @param inputStream File to get statistics.
     */
    public RootWordStatistics(FileInputStream inputStream){
        readFromFile(inputStream);
    }

    private void readFromFile(InputStream inputStream){
        String line, rootWord;
        String[] items;
        int size, count;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            size = Integer.parseInt(br.readLine());
            for (int i = 0; i < size; i++){
                line = br.readLine();
                items = line.split(" ");
                rootWord = items[0];
                count = Integer.parseInt(items[1]);
                CounterHashMap<String> map = new CounterHashMap<>();
                for (int j = 0; j < count; j++){
                    line = br.readLine();
                    items = line.split(" ");
                    map.putNTimes(items[0], Integer.parseInt(items[1]));
                }
                statistics.put(rootWord, map);
            }
            br.close();
        } catch (IOException e) {
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
        BufferedWriter fw;
        try {
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
            fw.write(statistics.size() + "\n");
            for (String rootWord : statistics.keySet()){
                CounterHashMap<String> map = statistics.get(rootWord);
                fw.write(rootWord + " " + map.size() + "\n");
                fw.write(map.toString());
            }
            fw.close();
        } catch (IOException e) {
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
