package MorphologicalDisambiguation;

import Classification.Attribute.Attribute;
import Classification.Attribute.AttributeType;
import Classification.Attribute.BinaryAttribute;
import Classification.Classifier.Classifier;
import Classification.Classifier.DiscreteFeaturesNotAllowed;
import Classification.Classifier.Dummy;
import Classification.DataSet.DataDefinition;
import Classification.DataSet.DataSet;
import Classification.Instance.Instance;
import Classification.Model.Model;
import Classification.Parameter.Parameter;
import Corpus.Sentence;
import Dictionary.Word;
import MorphologicalAnalysis.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RootFirstClassifierDisambiguation extends RootFirstDisambiguation{
    private HashMap<String, Model> models;
    private Classifier classifier;
    private Parameter parameters;

    public RootFirstClassifierDisambiguation(Classifier classifier, Parameter parameters){
        this.classifier = classifier;
        this.parameters = parameters;
    }

    private DataDefinition createDataDefinition(){
        ArrayList<AttributeType> attributeTypes = new ArrayList<>();
        for (int i = 0; i < 2 * InflectionalGroup.morphoTags.length; i++){
            attributeTypes.add(AttributeType.BINARY);
        }
        return new DataDefinition(attributeTypes);
    }

    private void addAttributes(InflectionalGroup ig, ArrayList<Attribute> attributes){
        for (int k = 0; k < InflectionalGroup.morphoTags.length; k++){
            if (ig.containsTag(InflectionalGroup.morphoTags[k])){
                attributes.add(new BinaryAttribute(true));
            } else {
                attributes.add(new BinaryAttribute(false));
            }
        }
    }

    private String classificationProblem(String disambiguationProblem, MorphologicalParse morphologicalParse){
        String[] parses = disambiguationProblem.split("\\$");
        for (int i = 0; i < parses.length; i++){
            if (morphologicalParse.toString().contains(parses[i])){
                return parses[i];
            }
        }
        return null;
    }

    public void train(DisambiguationCorpus corpus) {
        super.train(corpus);
        int i, j;
        Sentence sentence;
        Classifier currentClassifier;
        DataDefinition dataDefinition;
        DataSet dataSet;
        ArrayList<Attribute> attributes;
        InflectionalGroup ig, previousIg;
        HashMap<String, DataSet> dataSets = new HashMap<>();
        dataDefinition = createDataDefinition();
        FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
        for (i = 0; i < corpus.sentenceCount(); i++){
            sentence = corpus.getSentence(i);
            for (j = 2; j < sentence.wordCount(); j++){
                FsmParseList parseList = fsm.morphologicalAnalysis(sentence.getWord(j).getName());
                parseList.reduceToParsesWithSameRootAndPos(((DisambiguatedWord) sentence.getWord(j)).getParse().getWordWithPos());
                if (parseList.size() > 1){
                    String disambiguationProblem = parseList.parsesWithoutPrefixAndSuffix();
                    if (dataSets.containsKey(disambiguationProblem)){
                        dataSet = dataSets.get(disambiguationProblem);
                    } else {
                        dataSet = new DataSet(dataDefinition);
                        dataSets.put(disambiguationProblem, dataSet);
                    }
                    attributes = new ArrayList<>();
                    previousIg = ((DisambiguatedWord) sentence.getWord(j - 2)).getParse().lastInflectionalGroup();
                    addAttributes(previousIg, attributes);
                    ig = ((DisambiguatedWord) sentence.getWord(j - 1)).getParse().lastInflectionalGroup();
                    addAttributes(ig, attributes);
                    String classLabel = classificationProblem(disambiguationProblem, ((DisambiguatedWord) sentence.getWord(j)).getParse());
                    if (classLabel != null){
                        dataSet.addInstance(new Instance(classLabel, attributes));
                    }
                }
            }
            if (i > 0 && i % 5000 == 0){
                System.out.println("Trained " + i + " of sentences of " + corpus.sentenceCount());
            }
        }
        models = new HashMap<>();
        i = 0;
        for (String problem : dataSets.keySet()){
            if (dataSets.get(problem).sampleSize() >= 10) {
                currentClassifier = classifier;
            } else {
                currentClassifier = new Dummy();
            }
            try {
                currentClassifier.train(dataSets.get(problem).getInstanceList(), parameters);
                models.put(problem, currentClassifier.getModel());
                i++;
                System.out.println("Trained model " + i + " of " + dataSets.keySet().size());
            } catch (DiscreteFeaturesNotAllowed discreteFeaturesNotAllowed) {
                discreteFeaturesNotAllowed.printStackTrace();
            }
        }
    }

    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        int i;
        Word bestWord;
        FsmParse bestParse = null;
        ArrayList<Attribute> attributes;
        InflectionalGroup ig, previousIg;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<FsmParse>();
        for (i = 0; i < fsmParses.length; i++){
            bestWord = getBestRootWord(fsmParses[i]);
            fsmParses[i].reduceToParsesWithSameRootAndPos(bestWord);
            if (i < 2 || i != correctFsmParses.size()){
                bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
            } else {
                if (fsmParses[i].size() == 0){
                    bestParse = null;
                } else {
                    if (fsmParses[i].size() == 1){
                        bestParse = fsmParses[i].getFsmParse(0);
                    } else {
                        String disambiguationProblem = fsmParses[i].parsesWithoutPrefixAndSuffix();
                        if (models.containsKey(disambiguationProblem)){
                            Model model = models.get(disambiguationProblem);
                            attributes = new ArrayList<>();
                            previousIg = correctFsmParses.get(i - 2).lastInflectionalGroup();
                            addAttributes(previousIg, attributes);
                            ig = correctFsmParses.get(i - 1).lastInflectionalGroup();
                            addAttributes(ig, attributes);
                            String predictedParse = model.predict(new Instance("", attributes));
                            for (int j = 0; j < fsmParses[i].size(); j++){
                                if (fsmParses[i].getFsmParse(j).transitionList().contains(predictedParse)){
                                    bestParse = fsmParses[i].getFsmParse(j);
                                    break;
                                }
                            }
                            if (bestParse == null){
                                bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
                            }
                        } else {
                            bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
                        }
                    }
                }
            }
            if (bestParse != null){
                correctFsmParses.add(bestParse);
            }
        }
        return correctFsmParses;
    }

    public void saveModel() {
        super.saveModel();
        FileOutputStream outFile;
        ObjectOutputStream outObject;
        try {
            outFile = new FileOutputStream("classifiers.bin");
            outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(models);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        super.loadModel();
        ObjectInputStream inObject;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            inObject = new ObjectInputStream(classLoader.getResourceAsStream("classifiers.bin"));
            models = (HashMap<String, Model>) inObject.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
