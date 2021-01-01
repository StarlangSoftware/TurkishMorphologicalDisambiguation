package Annotation.Sentence;

import AnnotatedSentence.AnnotatedSentence;
import AnnotatedSentence.AnnotatedWord;
import AnnotatedSentence.ViewLayerType;
import AutoProcessor.Sentence.TurkishSentenceAutoDisambiguator;
import DataCollector.Sentence.SentenceAnnotatorPanel;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParseList;
import WordNet.WordNet;

import javax.swing.*;
import java.awt.*;

public class SentenceMorphologicalAnalyzerPanel extends SentenceAnnotatorPanel {
    private FsmMorphologicalAnalyzer fsm;
    private TurkishSentenceAutoDisambiguator turkishSentenceAutoDisambiguator;
    private WordNet wordNet;

    public SentenceMorphologicalAnalyzerPanel(String currentPath, String fileName, FsmMorphologicalAnalyzer fsm, WordNet wordNet, TurkishSentenceAutoDisambiguator turkishSentenceAutoDisambiguator){
        super(currentPath, fileName, ViewLayerType.INFLECTIONAL_GROUP);
        this.fsm = fsm;
        this.wordNet = wordNet;
        this.turkishSentenceAutoDisambiguator = turkishSentenceAutoDisambiguator;
        setLayout(new BorderLayout());
        list.setCellRenderer(new FsmParseListCellRenderer(wordNet));
        ToolTipManager.sharedInstance().registerComponent(list);
    }

    public void autoDetect(){
        turkishSentenceAutoDisambiguator.autoDisambiguate(sentence);
        sentence.save();
        this.repaint();
    }

    public void setFsm(FsmMorphologicalAnalyzer fsm){
        this.fsm = fsm;
    }

    public void setWordNet(WordNet wordNet){
        this.wordNet = wordNet;
    }

    public void setTurkishSentenceAutoDisambiguator(TurkishSentenceAutoDisambiguator turkishSentenceAutoDisambiguator){
        this.turkishSentenceAutoDisambiguator = turkishSentenceAutoDisambiguator;
    }

    public int populateLeaf(AnnotatedSentence sentence, int wordIndex){
        int selectedIndex = -1;
        AnnotatedWord word = (AnnotatedWord) sentence.getWord(wordIndex);
        listModel.clear();
        FsmParseList fsmParseList = fsm.robustMorphologicalAnalysis(word.getName());
        for (int i = 0; i < fsmParseList.size(); i++){
            if (word.getParse() != null && word.getParse().toString().equals(fsmParseList.getFsmParse(i).transitionList())){
                selectedIndex = i;
            }
            listModel.addElement(fsmParseList.getFsmParse(i));
        }
        return selectedIndex;
    }


}
