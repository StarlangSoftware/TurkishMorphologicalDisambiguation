package MorphologicalDisambiguation;

import Dictionary.Word;
import Ngram.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public abstract class NaiveDisambiguation implements MorphologicalDisambiguator{
    protected NGram<Word> wordUniGramModel;
    protected NGram<Word> igUniGramModel;

    public void saveModel() {
        wordUniGramModel.save("Model/words.1gram");
        igUniGramModel.save("Model/igs.1gram");
    }

    public void loadModel() {
        FileInputStream inFile;
        ObjectInputStream inObject;
        try {
            inFile = new FileInputStream("Model/words.1gram");
            inObject = new ObjectInputStream(inFile);
            wordUniGramModel = (NGram<Word>) inObject.readObject();
            inFile = new FileInputStream("Model/igs.1gram");
            inObject = new ObjectInputStream(inFile);
            igUniGramModel = (NGram<Word>) inObject.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
