package AutoProcessor.ParseTree;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import AnnotatedTree.Processor.Condition.IsTurkishLeafNode;
import AnnotatedTree.Processor.NodeDrawableCollector;
import AutoProcessor.ParseTree.PartOfSpeech.*;
import Corpus.Sentence;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import MorphologicalDisambiguation.LongestRootFirstDisambiguation;

import java.util.ArrayList;

public class TurkishTreeAutoDisambiguator extends TreeAutoDisambiguator {
    private final LongestRootFirstDisambiguation longestRootFirstDisambiguation;

    /**
     * Constructor for TurkishTreeAutoDisambiguator. Uses longest root first disambiguation strategy when needed.
     */
    public TurkishTreeAutoDisambiguator() {
        super(new FsmMorphologicalAnalyzer());
        longestRootFirstDisambiguation = new LongestRootFirstDisambiguation();
    }

    /**
     * The method checks all words in all leaves one by one. For each word, if that word has only one morphological
     * analysis, it sets the morphological layer to that morphological analysis.
     * @param parseTree Parse tree to be disambiguated.
     */
    protected void autoFillSingleAnalysis(ParseTreeDrawable parseTree){
        NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
        ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
        for (ParseNodeDrawable parseNode : leafList){
            if (parseNode.getLayerData(ViewLayerType.INFLECTIONAL_GROUP) == null){
                String turkishWords = parseNode.getLayerData(ViewLayerType.TURKISH_WORD);
                if (turkishWords != null){
                    String[] words = turkishWords.split(" ");
                    String morphologicalAnalysis = "", morphotactics = "";
                    for (String word : words){
                        FsmParseList fsmParseList = morphologicalAnalyzer.robustMorphologicalAnalysis(word);
                        if (fsmParseList.size() == 1){
                            morphologicalAnalysis = morphologicalAnalysis + " " + fsmParseList.getFsmParse(0).transitionList();
                            morphotactics = morphotactics + " " + fsmParseList.getFsmParse(0).withList();
                        } else {
                            morphologicalAnalysis = "";
                            morphotactics = "";
                            break;
                        }
                    }
                    if (!morphologicalAnalysis.isEmpty()){
                        parseNode.getLayerInfo().setLayerData(ViewLayerType.INFLECTIONAL_GROUP, morphologicalAnalysis.trim());
                        parseNode.getLayerInfo().setLayerData(ViewLayerType.META_MORPHEME, morphotactics.trim());
                    }
                }
            }
        }
    }

    /**
     * Given the correct parses of all word for a parse node, the method sets the morphological layer for that node. The
     * morphological parses of all words in that node should be separated with space character.
     * @param disambiguatedFsmParses Correct morphological parses for all words in the parse node.
     * @param parseNode Parse node to set morphological layer.
     */
    private void setDisambiguatedParses(FsmParse[] disambiguatedFsmParses, ParseNodeDrawable parseNode){
        StringBuilder morphologicalAnalysis = new StringBuilder(disambiguatedFsmParses[0].transitionList());
        StringBuilder morphotactics = new StringBuilder(disambiguatedFsmParses[0].withList());
        for (int i = 1; i < disambiguatedFsmParses.length; i++){
            morphologicalAnalysis.append(" ").append(disambiguatedFsmParses[i].transitionList());
            morphotactics.append(" ").append(disambiguatedFsmParses[i].withList());
        }
        parseNode.getLayerInfo().setLayerData(ViewLayerType.INFLECTIONAL_GROUP, morphologicalAnalysis.toString());
        parseNode.getLayerInfo().setLayerData(ViewLayerType.META_MORPHEME, morphotactics.toString());
    }

    /**
     * If the possible morphological analyses of the words contain more than one root word, this method calls
     * longest root word disambiguation algorithm for each word to disambiguate morphological analyses of that word.
     * @param parseTree Parse tree to be disambiguated.
     */
    protected void autoDisambiguateMultipleRootWords(ParseTreeDrawable parseTree) {
        NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
        ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
        for (ParseNodeDrawable parseNode : leafList) {
            if (parseNode.getLayerData(ViewLayerType.INFLECTIONAL_GROUP) == null) {
                String turkishWords = parseNode.getLayerData(ViewLayerType.TURKISH_WORD);
                if (turkishWords != null) {
                    FsmParseList[] fsmParseLists = morphologicalAnalyzer.robustMorphologicalAnalysis(new Sentence(turkishWords));
                    ArrayList<FsmParse> fsmParses = longestRootFirstDisambiguation.disambiguate(fsmParseLists);
                    FsmParse[] disambiguatedParses = new FsmParse[fsmParses.size()];
                    for (int i = 0; i < fsmParses.size(); i++){
                        disambiguatedParses[i] = fsmParses.get(i);
                    }
                    setDisambiguatedParses(disambiguatedParses, parseNode);
                }
            }
        }
    }

    /**
     * The method checks all leaves one by one. Depending on the pos tag of the parent node of a leaf node, the method
     * calls different rule based disambiguators. For adverbs, TurkishRBDisambiguator is used; for pronouns,
     * TurkishPRPDisambiguator is used; for nouns, TurkishNNPDisambiguator and TurkishNNDisambiguator are used;
     * for adjectives, TurkishJJDisambiguator is used; for verbs, TurkishVBDisambiguator is used. For prepositions,
     * TurkishINTODisambiguator is used; for conjunctions, TurkishCCDisambiguator is used; for numbers,
     * TurkishCDDisambiguator is used; for determiners, TurkishDTDisambiguator is used.
     * @param parseTree Parse tree to be disambiguated.
     */
    protected void autoDisambiguateWithRules(ParseTreeDrawable parseTree) {
        PartOfSpeechDisambiguator disambiguator;
        FsmParse[] disambiguatedFsmParses;
        NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
        ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
        for (int i = 0; i < leafList.size(); i++){
            ParseNodeDrawable parseNode = leafList.get(i);
            if (parseNode.getLayerData(ViewLayerType.INFLECTIONAL_GROUP) == null){
                String turkishWords = parseNode.getLayerData(ViewLayerType.TURKISH_WORD);
                if (turkishWords != null) {
                    FsmParseList[] fsmParseList = morphologicalAnalyzer.robustMorphologicalAnalysis(new Sentence(turkishWords));
                    switch (parseNode.getParent().getData().getName()){
                        case "RB":
                        case "RBR":
                        case "RBS":
                            disambiguator = new TurkishRBDisambiguator();
                            break;
                        case "IN":
                        case "TO":
                            disambiguator = new TurkishINTODisambiguator();
                            break;
                        case "CC":
                            disambiguator = new TurkishCCDisambiguator();
                            break;
                        case "WDT":
                        case "DT":
                            disambiguator = new TurkishDTDisambiguator();
                            break;
                        case "CD":
                            disambiguator = new TurkishCDDisambiguator();
                            break;
                        case "PRP":
                        case "PRP$":
                        case "WP":
                        case "WP$":
                            disambiguator = new TurkishPRPDisambiguator();
                            break;
                        case "NNP":
                        case "NNPS":
                            disambiguator = new TurkishNNPDisambiguator();
                            break;
                        case "NN":
                        case "NNS":
                            disambiguator = new TurkishNNDisambiguator();
                            break;
                        case "JJ":
                        case "JJR":
                        case "JJS":
                            disambiguator = new TurkishJJDisambiguator();
                            break;
                        case "$":
                            disambiguator = new TurkishDollarDisambiguator();
                            break;
                        case "VBN":
                        case "VBZ":
                        case "VBD":
                        case "VB":
                        case "VBG":
                        case "VBP":
                            if (TurkishPartOfSpeechDisambiguator.isLastNode(i, leafList)){
                                disambiguator = new TurkishVBDisambiguator();
                            } else {
                                disambiguator = null;
                            }
                            break;
                        default:
                            disambiguator = null;
                    }
                    if (disambiguator != null){
                        disambiguatedFsmParses = disambiguator.disambiguate(fsmParseList, parseNode, parseTree);
                        if (disambiguatedFsmParses != null){
                            setDisambiguatedParses(disambiguatedFsmParses, parseNode);
                        }
                    }
                }
            }
        }
    }

}
