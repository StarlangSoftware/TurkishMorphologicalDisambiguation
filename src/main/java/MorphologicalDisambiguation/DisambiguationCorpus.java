package MorphologicalDisambiguation;

import Corpus.*;
import DataStructure.CounterHashMap;
import MorphologicalAnalysis.MorphologicalParse;
import java.io.*;
import java.util.ArrayList;

public class DisambiguationCorpus extends Corpus{

    public DisambiguationCorpus(){
        sentences = new ArrayList<>();
        wordList = new CounterHashMap<>();
    }

    public DisambiguationCorpus emptyCopy(){
        return new DisambiguationCorpus();
    }

    public DisambiguationCorpus(String fileName){
        super();
        int i = 1;
        String line, word, parse;
        DisambiguatedWord newWord;
        Sentence newSentence = null;
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            line = br.readLine();
            while (line != null){
                word = line.substring(0, line.indexOf("\t"));
                parse = line.substring(line.indexOf("\t") + 1);
                if (!word.isEmpty() && !parse.isEmpty()){
                    newWord = new DisambiguatedWord(word, new MorphologicalParse(parse));
                    if (word.equals("<S>")){
                        newSentence = new Sentence();
                    } else {
                        if (word.equals("</S>")){
                            addSentence(newSentence);
                        } else {
                            if (word.equals("<DOC>") || word.equals("</DOC>") || word.equals("<TITLE>") || word.equals("</TITLE>")){
                            } else {
                                if (newSentence != null){
                                    newSentence.addWord(newWord);
                                } else {
                                    System.out.println("Word " + word + " out of place\n");
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Not enough items in " + line + " " + i + "\n");
                }
                i++;
                if (i % 10000 == 0){
                    System.out.println("Read " + i + " items");
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String fileName){
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
            writer.println("<DOC>\t<DOC>+BDTag");
            for (Sentence sentence : sentences){
                writer.println("<S>\t<S>+BSTag");
                for (int i = 0; i < sentence.wordCount(); i++){
                    DisambiguatedWord word = (DisambiguatedWord) sentence.getWord(i);
                    writer.println(word.getName() + "\t" + word.getParse());
                }
                writer.println("</S>\t</S>+ESTag");
            }
            writer.println("</DOC>\t</DOC>+EDTag");
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String fileName, WordFormat format){
        if (format.equals(WordFormat.SURFACE) || format.equals(WordFormat.LETTER_2) || format.equals(WordFormat.LETTER_3) || format.equals(WordFormat.LETTER_4)){
            super.writeToFile(fileName, format);
        }
    }

}
