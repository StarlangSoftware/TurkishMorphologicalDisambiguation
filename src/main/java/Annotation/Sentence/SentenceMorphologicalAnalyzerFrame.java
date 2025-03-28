package Annotation.Sentence;

import AnnotatedSentence.AnnotatedCorpus;
import AutoProcessor.Sentence.TurkishSentenceAutoDisambiguator;
import DataCollector.Sentence.SentenceAnnotatorFrame;
import DataCollector.Sentence.SentenceAnnotatorPanel;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.WordNet;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

public class SentenceMorphologicalAnalyzerFrame extends SentenceAnnotatorFrame {
    private final JCheckBox autoAnalysisDetectionOption;
    private FsmMorphologicalAnalyzer fsm;
    private WordNet wordNet;
    private TurkishSentenceAutoDisambiguator turkishSentenceAutoDisambiguator;

    /**
     * Constructor of the morphological disambiguation frame for annotated sentence. It reads the annotated sentence
     * corpus and adds automatic morphological disambiguation button. It also adds itemUpdateDictionary button whose
     * purpose is to use alternative domain dictionary and wordnet for morphological disambiguation.
     * @param fsm Morphological analyzer
     * @param wordNet Turkish wordnet
     */
    public SentenceMorphologicalAnalyzerFrame(final FsmMorphologicalAnalyzer fsm, final WordNet wordNet){
        super();
        this.fsm = fsm;
        this.wordNet = wordNet;
        AnnotatedCorpus corpus;
        String subFolder = "false";
        Properties properties1 = new Properties();
        try {
            properties1.load(Files.newInputStream(new File("config.properties").toPath()));
            if (properties1.containsKey("subFolder")){
                subFolder = properties1.getProperty("subFolder");
            }
        } catch (IOException ignored) {
        }
        corpus = readCorpus(subFolder);
        JMenuItem itemUpdateDictionary = addMenuItem(projectMenu, "Update Analyzer", KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
        itemUpdateDictionary.addActionListener(e -> {
            Properties properties = new Properties();
            try {
                properties.load(Files.newInputStream(new File("config.properties").toPath()));
                String domainPrefix = properties.getProperty("domainPrefix");
                String domainDictionaryFileName = domainPrefix + "_dictionary.txt";
                String wordNetFileName = domainPrefix + "_wordnet.txt";
                this.fsm = new FsmMorphologicalAnalyzer(domainDictionaryFileName);
                this.wordNet = new WordNet(wordNetFileName, new Locale("tr"));
                turkishSentenceAutoDisambiguator = new TurkishSentenceAutoDisambiguator(this.fsm);
            } catch (IOException ignored) {
            }
            for (int i = 0; i < projectPane.getTabCount(); i++){
                SentenceMorphologicalAnalyzerPanel current = (SentenceMorphologicalAnalyzerPanel) ((JScrollPane) projectPane.getComponentAt(i)).getViewport().getView();
                current.setFsm(this.fsm);
                current.setWordNet(this.wordNet);
                current.setTurkishSentenceAutoDisambiguator(turkishSentenceAutoDisambiguator);
            }
        });
        autoAnalysisDetectionOption = new JCheckBox("Auto Morphological Disambiguation", false);
        toolBar.add(autoAnalysisDetectionOption);
        turkishSentenceAutoDisambiguator = new TurkishSentenceAutoDisambiguator();
        JMenuItem itemViewAnnotated = addMenuItem(projectMenu, "View Annotations", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        itemViewAnnotated.addActionListener(e -> new ViewSentenceMorphologicalAnnotationFrame(corpus, this));
        JOptionPane.showMessageDialog(this, "WordNet, dictionary and annotated corpus are loaded!", "Morphological Annotation", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected SentenceAnnotatorPanel generatePanel(String currentPath, String rawFileName) {
        return new SentenceMorphologicalAnalyzerPanel(currentPath, rawFileName, fsm, wordNet, turkishSentenceAutoDisambiguator);
    }

    /**
     * The next method takes an int count as input and moves forward along the SentenceMorphologicalAnalyzerPanels as
     * much as the count. If the autoAnalysisDetectionOption is selected, it morphologically disambiguates
     * words automatically.
     * @param count Integer count is used to move forward.
     */
    public void next(int count){
        super.next(count);
        SentenceMorphologicalAnalyzerPanel current;
        current = (SentenceMorphologicalAnalyzerPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
        if (autoAnalysisDetectionOption.isSelected()){
            current.autoDetect();
        }
    }

    /**
     * The previous method takes an int count as input and moves backward along the SentenceMorphologicalAnalyzerPanels
     * as much as the count. If the autoAnalysisDetectionOption is selected, it morphologically
     * disambiguates words automatically.
     * @param count Integer count is used to move backward.
     */
    public void previous(int count){
        super.previous(count);
        SentenceMorphologicalAnalyzerPanel current;
        current = (SentenceMorphologicalAnalyzerPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
        if (autoAnalysisDetectionOption.isSelected()){
            current.autoDetect();
        }
    }

}
