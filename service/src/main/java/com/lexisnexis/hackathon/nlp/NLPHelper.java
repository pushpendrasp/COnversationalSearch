package com.lexisnexis.hackathon.nlp;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class NLPHelper {

    private StanfordCoreNLP pipeline;

    public NLPHelper() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<List<String>> extract(String text) {

        List<List<String>> tokenLists = new ArrayList<>();
        List<String> tokenList = new ArrayList<String>();
        List<String> personList = new ArrayList<String>();
        List<String> numberList = new ArrayList<String>();
        List<String> otherList = new ArrayList<String>();

        Annotation document = new Annotation(text);

        this.pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if (pos.startsWith("N")) {
                    if (ne.equalsIgnoreCase("PERSON")) {
                        personList.add(word);
                    } else {
                        tokenList.add(word);
                    }
                } else {
                    if (ne.equalsIgnoreCase("NUMBER")) {
                        numberList.add(word);
                    } else {
                        otherList.add(word);
                    }
                }
                System.out.println("word: " + word + " pos: " + pos + " ne:" + ne);

            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                document.get(CorefCoreAnnotations.CorefChainAnnotation.class);

        System.out.println(graph);

        tokenLists.add(personList);
        tokenLists.add(tokenList);
        tokenLists.add(numberList);
        tokenLists.add(otherList);
        return tokenLists;
    }

    public static void main(String[] args) {
        NLPHelper nlpHelper = new NLPHelper();
        nlpHelper.extract("Who was the judge for ABCD case?");
    }


}
