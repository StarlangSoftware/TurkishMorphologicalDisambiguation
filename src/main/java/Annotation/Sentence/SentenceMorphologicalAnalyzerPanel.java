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

    /**
     * Constructor for the morphological disambiguator panel for an annotated sentence. Sets the attributes.
     * @param currentPath The absolute path of the annotated file.
     * @param fileName The raw file name of the annotated file.
     * @param fsm Morphological analyzer
     * @param wordNet Turkish Wordnet
     * @param turkishSentenceAutoDisambiguator Morphological disambiguator
     */
    public SentenceMorphologicalAnalyzerPanel(String currentPath, String fileName, FsmMorphologicalAnalyzer fsm, WordNet wordNet, TurkishSentenceAutoDisambiguator turkishSentenceAutoDisambiguator){
        super(currentPath, fileName, ViewLayerType.INFLECTIONAL_GROUP);
        this.fsm = fsm;
        this.turkishSentenceAutoDisambiguator = turkishSentenceAutoDisambiguator;
        setLayout(new BorderLayout());
        list.setCellRenderer(new FsmParseListCellRenderer(wordNet));
        ToolTipManager.sharedInstance().registerComponent(list);
    }

    /**
     * Updates the morphological analysis and metamorpheme layer of the annotated word.
     */
    @Override
    protected void setWordLayer() {
        clickedWord.setParse(((FsmParse)list.getSelectedValue()).transitionList());
        clickedWord.setMetamorphicParse(((FsmParse)list.getSelectedValue()).withList());
    }

    /**
     * Sets the width and height of the JList that displays the morphological analysis.
     */
    @Override
    protected void setBounds() {
        pane.setBounds(((AnnotatedWord)sentence.getWord(selectedWordIndex)).getArea().getX(), ((AnnotatedWord)sentence.getWord(selectedWordIndex)).getArea().getY() + 20, 240, (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.4));
    }

    /**
     * Sets the space between displayed lines in the sentence.
     */
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

    /**
     * Draws the morphological analysis of the word.
     * @param word Annotated word itself.
     * @param g Graphics on which morphological analysis is drawn.
     * @param currentLeft Current position on the x-axis, where the morphological analysis will be aligned.
     * @param lineIndex Current line of the word, if the sentence resides in multiple lines on the screen.
     * @param wordIndex Index of the word in the annotated sentence.
     * @param maxSize Maximum size in pixels of anything drawn in the screen.
     * @param wordSize Array storing the sizes of all words in pixels in the annotated sentence.
     * @param wordTotal Array storing the total size until that word of all words in the annotated sentence.
     */
    @Override
    protected void drawLayer(AnnotatedWord word, Graphics g, int currentLeft, int lineIndex, int wordIndex, int maxSize, ArrayList<Integer> wordSize, ArrayList<Integer> wordTotal) {
        if (word.getParse() != null){
            for (int j = 0; j < word.getParse().size(); j++){
                g.drawString(word.getParse().getInflectionalGroupString(j), currentLeft, (lineIndex + 1) * lineSpace + 30 * (j + 1));
            }
        }
    }

    /**
     * Compares the size of the word and the maximum size of the inflectional groups in pixels and returns the maximum
     * of them.
     * @param word Word annotated.
     * @param g Graphics on which morphological analysis is drawn.
     * @return Maximum of the graphic sizes of word and its morphological analysis.
     */
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

    /**
     * Automatically disambiguate words in the sentence using turkishSentenceAutoDisambiguator.
     */
    public void autoDetect(){
        turkishSentenceAutoDisambiguator.autoDisambiguate(sentence);
        sentence.save();
        this.repaint();
    }

    /**
     * Mutator for fsm attribute
     * @param fsm New morphological analyzer
     */
    public void setFsm(FsmMorphologicalAnalyzer fsm){
        this.fsm = fsm;
    }

    /**
     * Mutator for wordNet attribute
     * @param wordNet New wordnet
     */
    public void setWordNet(WordNet wordNet){
        list.setCellRenderer(new FsmParseListCellRenderer(wordNet));
    }

    /**
     * Mutator for morphological disambiguator
     * @param turkishSentenceAutoDisambiguator New morphological disambiguator
     */
    public void setTurkishSentenceAutoDisambiguator(TurkishSentenceAutoDisambiguator turkishSentenceAutoDisambiguator){
        this.turkishSentenceAutoDisambiguator = turkishSentenceAutoDisambiguator;
    }

    /**
     * Fills the JList that contains all possible morphological analyses.
     * @param sentence Sentence used to populate for the current word.
     * @param wordIndex Index of the selected word.
     * @return The index of the selected morphological analysis, -1 if nothing selected.
     */
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
