package com.dicoding.summariz_it.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class PreProcessor {
    public static final String[] STOP_WORDS = new String[]{"a", "able", "about", "after", "all", "also", "am",
            "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "did",
            "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her",
            "hers", "him", "his", "how", "I", "if", "in", "into", "is", "it", "its", "just", "let", "like", "likely", "may", "me",
            "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off",
            "often", "on", "only", "or", "other", "our", "own", "said", "say", "says", "she",
            "should", "so", "some", "than", "that", "the", "their", "them", "then", "there",
            "these", "they", "this", "they're", "to", "too", "that's", "us", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with",
            "would", "yet", "you", "your", "you're",
            "s", "ve", "d", "nt"
    };
    private final String TAG = "INFO";
    private final SentenceDetectorME sentenceDetector;
    private final Tokenizer tokenizer;
    private final DictionaryLemmatizer lemming;
    private final POSTaggerME posTagger;

    public PreProcessor(InputStream sentenceModel, InputStream tokenizerModel, InputStream lemmaModel, InputStream pos) throws IOException {
        SentenceModel sentModel = new SentenceModel(sentenceModel);
        sentenceDetector = new SentenceDetectorME(sentModel);
        TokenizerModel tokenModel = new TokenizerModel(tokenizerModel);
        tokenizer = new TokenizerME(tokenModel);
        POSModel posModel = new POSModel(pos);
        posTagger = new POSTaggerME(posModel);
        lemming = new DictionaryLemmatizer(lemmaModel);
    }

    public String[] extractSentences(String documentText) {
        String[] sentences = sentenceDetector.sentDetect(documentText.trim());

        Log.i(TAG, "extractSentences: Clean sentences");
        for (int i = 0; i < sentences.length; i++) {
            sentences[i] = sentences[i].trim().replace("\n", " ").replace("\r", " ");
            Log.i(TAG, sentences[i]);
            Log.i(TAG, "===================================");
        }
        return sentences;
    }

    public String[][] tokenizeSentences(String[] sent) {
        String[][] tokenized = new String[sent.length][];
        String stopWordsPattern = TextUtils.join("|", STOP_WORDS);
        Pattern pattern = Pattern.compile("\\b(?:" + stopWordsPattern + ")\\b\\s*", Pattern.CASE_INSENSITIVE);
        Log.i(TAG, "tokenizeSentences: Tokenized, Summarized and Stop words removed");
        for (int i = 0; i < sent.length; i++) {
            tokenized[i] = tokenizer.tokenize(sent[i]);
            tokenized[i] = lemmingsTokens(tokenized[i]);
            for (int j = 0; j < tokenized[i].length; j++) {
                Matcher matcher = pattern.matcher(tokenized[i][j]);
                tokenized[i][j] = matcher.replaceAll("O");
            }
        }
        return tokenized;
    }

    public String[] lemmingsTokens(String[] tokens) {
        String[] tags = posTagger.tag(tokens);
        return lemming.lemmatize(tokens, tags);
    }
}
