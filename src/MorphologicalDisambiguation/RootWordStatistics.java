package MorphologicalDisambiguation;

import DataStructure.CounterHashMap;
import MorphologicalAnalysis.FsmParseList;

import java.io.*;
import java.util.HashMap;

public class RootWordStatistics implements Serializable{
    private HashMap<String, CounterHashMap<String>> statistics;

    public RootWordStatistics(){
        statistics = new HashMap<String, CounterHashMap<String>>();
    }

    public RootWordStatistics(String fileName){
        FileInputStream inFile;
        ObjectInputStream inObject;
        try {
            inFile = new FileInputStream(fileName);
            inObject = new ObjectInputStream(inFile);
            statistics = (HashMap<String, CounterHashMap<String>>) inObject.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public boolean containsKey(String key){
        return statistics.containsKey(key);
    }

    public CounterHashMap<String> get(String key){
        return statistics.get(key);
    }

    public void put(String key, CounterHashMap<String> wordStatistics){
        statistics.put(key, wordStatistics);
    }

    public void saveStatistics(String fileName){
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

    public String bestRootWord(FsmParseList parseList, double threshold){
        String rootWords = parseList.rootWords();
        if (statistics.containsKey(rootWords)){
            CounterHashMap<String> rootWordStatistics = statistics.get(rootWords);
            return rootWordStatistics.max(threshold);
        }
        return null;
    }

}
