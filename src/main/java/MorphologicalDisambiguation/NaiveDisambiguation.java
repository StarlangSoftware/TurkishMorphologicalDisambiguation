package MorphologicalDisambiguation;

import Dictionary.Word;
import Ngram.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public abstract class NaiveDisambiguation implements MorphologicalDisambiguator {
    protected NGram<Word> wordUniGramModel;
    protected NGram<Word> igUniGramModel;

    /**
     * The saveModel method writes the specified objects i.e wordUniGramModel and igUniGramModel to the
     * words1.txt and igs1.txt.
     */
    public void saveModel() {
        wordUniGramModel.saveAsText("words1.txt");
        igUniGramModel.saveAsText("igs1.txt");
    }

    /**
     * The loadModel method reads objects at the words1.txt and igs1.txt to the wordUniGramModel and igUniGramModel.
     */
    public void loadModel() {
        wordUniGramModel = new NGram<>("words1.txt");
        igUniGramModel = new NGram<>("igs1.txt");
    }


}
