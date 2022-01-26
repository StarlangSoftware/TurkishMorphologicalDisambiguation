package Annotation.Sentence;

import AnnotatedSentence.AnnotatedSentence;
import AnnotatedSentence.AnnotatedWord;
import AnnotatedSentence.ViewLayerType;
import AutoProcessor.Sentence.TurkishSentenceAutoDisambiguator;
import DataCollector.Sentence.SentenceAnnotatorPanel;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import WordNet.WordNet;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

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

    @Override
    protected void setWordLayer() {
        clickedWord.setParse(((FsmParse)list.getSelectedValue()).transitionList());
        clickedWord.setMetamorphicParse(((FsmParse)list.getSelectedValue()).withList());
    }

    @Override
    protected void setBounds() {
        pane.setBounds(((AnnotatedWord)sentence.getWord(selectedWordIndex)).getArea().x, ((AnnotatedWord)sentence.getWord(selectedWordIndex)).getArea().y + 20, 240, (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.4));
    }

    @Override
    protected void setLineSpace() {
        int maxSize = 1;
        for (int i = 0; i < sentence.wordCount(); i++){
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getParse() != null && word.getParse().size() > maxSize){
                maxSize = word.getParse().size();
            }
        }
        lineSpace = 40 * (maxSize + 1);
    }

    @Override
    protected void drawLayer(AnnotatedWord word, Graphics g, int currentLeft, int lineIndex, int wordIndex, int maxSize, ArrayList<Integer> wordSize, ArrayList<Integer> wordTotal) {
        if (word.getParse() != null){
            for (int j = 0; j < word.getParse().size(); j++){
                g.drawString(word.getParse().getInflectionalGroupString(j), currentLeft, (lineIndex + 1) * lineSpace + 30 * (j + 1));
            }
        }
    }

    @Override
    protected int getMaxLayerLength(AnnotatedWord word, Graphics g) {
        int maxSize = g.getFontMetrics().stringWidth(word.getName());
        if (word.getParse() != null){
            for (int j = 0; j < word.getParse().size(); j++){
                int size = g.getFontMetrics().stringWidth(word.getParse().getInflectionalGroupString(j));
                if (size > maxSize){
                    maxSize = size;
                }
            }
        }
        return maxSize;
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
