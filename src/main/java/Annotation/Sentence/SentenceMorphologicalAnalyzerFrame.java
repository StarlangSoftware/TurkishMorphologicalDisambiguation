package Annotation.Sentence;

import AnnotatedSentence.AnnotatedCorpus;
import AutoProcessor.Sentence.TurkishSentenceAutoDisambiguator;
import DataCollector.ParseTree.TreeEditorPanel;
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

    public SentenceMorphologicalAnalyzerFrame(final FsmMorphologicalAnalyzer fsm, final WordNet wordNet){
        super();
        this.fsm = fsm;
        this.wordNet = wordNet;
        AnnotatedCorpus corpus;
        corpus = new AnnotatedCorpus(new File(TreeEditorPanel.phrasePath));
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
        itemViewAnnotated.addActionListener(e -> new ViewSentenceMorphologicalAnnotationFrame(fsm, corpus, this));
        JOptionPane.showMessageDialog(this, "WordNet, dictionary and annotated corpus are loaded!", "Morphological Annotation", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected SentenceAnnotatorPanel generatePanel(String currentPath, String rawFileName) {
        return new SentenceMorphologicalAnalyzerPanel(currentPath, rawFileName, fsm, wordNet, turkishSentenceAutoDisambiguator);
    }

    public void next(int count){
        super.next(count);
        SentenceMorphologicalAnalyzerPanel current;
        current = (SentenceMorphologicalAnalyzerPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
        if (autoAnalysisDetectionOption.isSelected()){
            current.autoDetect();
        }
    }

    public void previous(int count){
        super.previous(count);
        SentenceMorphologicalAnalyzerPanel current;
        current = (SentenceMorphologicalAnalyzerPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
        if (autoAnalysisDetectionOption.isSelected()){
            current.autoDetect();
        }
    }

}
