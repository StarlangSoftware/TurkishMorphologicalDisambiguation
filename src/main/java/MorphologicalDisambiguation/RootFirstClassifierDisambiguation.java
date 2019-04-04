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

public class RootFirstClassifierDisambiguation extends RootFirstDisambiguation {
    private HashMap<String, Model> models;
    private Classifier classifier;
    private Parameter parameters;

    /**
     * Constructor for setting the {@link Classifier} and {@link Parameter}.
     *
     * @param classifier Type of the {@link Classifier}.
     * @param parameters {@link Parameter}s of the classifier.
     */
    public RootFirstClassifierDisambiguation(Classifier classifier, Parameter parameters) {
        this.classifier = classifier;
        this.parameters = parameters;
    }

    /**
     * The createDataDefinition method creates an {@link ArrayList} of {@link AttributeType}s and adds 2 times BINARY AttributeType
     * for each element of the {@link InflectionalGroup}.
     *
     * @return A new data definition with the attributeTypes.
     */
    private DataDefinition createDataDefinition() {
        ArrayList<AttributeType> attributeTypes = new ArrayList<>();
        for (int i = 0; i < 2 * InflectionalGroup.morphoTags.length; i++) {
            attributeTypes.add(AttributeType.BINARY);
        }
        return new DataDefinition(attributeTypes);
    }

    /**
     * The addAttributes method takes an {@link InflectionalGroup} ig and an {@link ArrayList} of attributes. If the given
     * ig contains any of the morphological tags of InflectionalGroup, it adds a new {@link BinaryAttribute} with the value of
     * true to the attributes ArrayList, if not it adds a new {@link BinaryAttribute} with the value of false.
     *
     * @param ig         InflectionalGroup to check the morphological tags.
     * @param attributes ArrayList of attributes.
     */
    private void addAttributes(InflectionalGroup ig, ArrayList<Attribute> attributes) {
        for (int k = 0; k < InflectionalGroup.morphoTags.length; k++) {
            if (ig.containsTag(InflectionalGroup.morphoTags[k])) {
                attributes.add(new BinaryAttribute(true));
            } else {
                attributes.add(new BinaryAttribute(false));
            }
        }
    }

    /**
     * The classificationProblem method takes a {@link String} input and parses it. If the given {@link MorphologicalParse}
     * contains the parsed String, it directly returns that String, if not it returns null.
     *
     * @param disambiguationProblem String input to be parsed.
     * @param morphologicalParse    MorphologicalParse input.
     * @return If the given {MorphologicalParse contains the String, it directly returns that String, if not it returns null.
     */
    private String classificationProblem(String disambiguationProblem, MorphologicalParse morphologicalParse) {
        String[] parses = disambiguationProblem.split("\\$");
        for (String parse : parses) {
            if (morphologicalParse.toString().contains(parse)) {
                return parse;
            }
        }
        return null;
    }

    /**
     * The train method gets sentences from given {@link DisambiguationCorpus}, then perform morphological analyses for each
     * word of a sentence and gets a {@link FsmParseList} at each time and removes the other words which are identical to the current word
     * and part of speech tags.
     * <p>
     * If the size of the {@link FsmParseList} greater than 1,  it removes the prefixes and suffixes from the {@link FsmParseList} and
     * evaluates it as disambiguationProblem  String. If this String is already placed in Dataset, it gets its value, else
     * put it to the Dataset as a new key.
     * <p>
     * Apart from that, it also gets two previous InflectionalGroups and finds out their class labels, and adds them to the Dataset
     * as a new {@link Instance}.
     *
     * @param corpus {@link DisambiguationCorpus} to train.
     */
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
        for (i = 0; i < corpus.sentenceCount(); i++) {
            sentence = corpus.getSentence(i);
            for (j = 2; j < sentence.wordCount(); j++) {
                FsmParseList parseList = fsm.morphologicalAnalysis(sentence.getWord(j).getName());
                parseList.reduceToParsesWithSameRootAndPos(((DisambiguatedWord) sentence.getWord(j)).getParse().getWordWithPos());
                if (parseList.size() > 1) {
                    String disambiguationProblem = parseList.parsesWithoutPrefixAndSuffix();
                    if (dataSets.containsKey(disambiguationProblem)) {
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
                    if (classLabel != null) {
                        dataSet.addInstance(new Instance(classLabel, attributes));
                    }
                }
            }
            if (i > 0 && i % 5000 == 0) {
                System.out.println("Trained " + i + " of sentences of " + corpus.sentenceCount());
            }
        }
        models = new HashMap<>();
        i = 0;
        for (String problem : dataSets.keySet()) {
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

    /**
     * The disambiguate method gets an array of fsmParses. Then loops through these parses and finds the most probable root
     * word and removes the other words which are identical to the most probable root word. For the first two items and
     * the last item, it gets the most probable ig parse among the fsmParses and adds it to the correctFsmParses {@link ArrayList} and returns it.
     * For the other cases, it gets the classification model,  considering the previous two ig it performs a prediction
     * and at the end returns the correctFsmParses that holds the best parses.
     *
     * @param fsmParses {@link FsmParseList} to disambiguate.
     * @return The correctFsmParses that holds the best parses.
     */
    public ArrayList<FsmParse> disambiguate(FsmParseList[] fsmParses) {
        int i;
        Word bestWord;
        FsmParse bestParse = null;
        ArrayList<Attribute> attributes;
        InflectionalGroup ig, previousIg;
        ArrayList<FsmParse> correctFsmParses = new ArrayList<FsmParse>();
        for (i = 0; i < fsmParses.length; i++) {
            bestWord = getBestRootWord(fsmParses[i]);
            fsmParses[i].reduceToParsesWithSameRootAndPos(bestWord);
            if (i < 2 || i != correctFsmParses.size()) {
                bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
            } else {
                if (fsmParses[i].size() == 0) {
                    bestParse = null;
                } else {
                    if (fsmParses[i].size() == 1) {
                        bestParse = fsmParses[i].getFsmParse(0);
                    } else {
                        String disambiguationProblem = fsmParses[i].parsesWithoutPrefixAndSuffix();
                        if (models.containsKey(disambiguationProblem)) {
                            Model model = models.get(disambiguationProblem);
                            attributes = new ArrayList<>();
                            previousIg = correctFsmParses.get(i - 2).lastInflectionalGroup();
                            addAttributes(previousIg, attributes);
                            ig = correctFsmParses.get(i - 1).lastInflectionalGroup();
                            addAttributes(ig, attributes);
                            String predictedParse = model.predict(new Instance("", attributes));
                            for (int j = 0; j < fsmParses[i].size(); j++) {
                                if (fsmParses[i].getFsmParse(j).transitionList().contains(predictedParse)) {
                                    bestParse = fsmParses[i].getFsmParse(j);
                                    break;
                                }
                            }
                            if (bestParse == null) {
                                bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
                            }
                        } else {
                            bestParse = getParseWithBestIgProbability(fsmParses[i], correctFsmParses, i);
                        }
                    }
                }
            }
            if (bestParse != null) {
                correctFsmParses.add(bestParse);
            }
        }
        return correctFsmParses;
    }

    /**
     * Method to save unigrams and bigrams.
     */
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

    /**
     * Method to load unigrams and bigrams.
     */
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
