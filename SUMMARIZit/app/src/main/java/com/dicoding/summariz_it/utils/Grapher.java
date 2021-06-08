package com.dicoding.summariz_it.utils;

import android.util.Log;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Grapher {
    SimpleWeightedGraph<SentenceVertex, DefaultWeightedEdge> graph;

    public Grapher() {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public void makeGraph(String[] sentences, String[][] tokens) {
        for (int i = 0; i < sentences.length; i++) {
            SentenceVertex sentVertex = new SentenceVertex(sentences[i], tokens[i]);
            graph.addVertex(sentVertex);
        }
        for (SentenceVertex vertex1 : graph.vertexSet()) {
            for (SentenceVertex vertex2 : graph.vertexSet()) {
                if (vertex1 != vertex2) {
                    double weight = calcSimilarity(vertex1, vertex2);
                    graph.addEdge(vertex1, vertex2);
                    DefaultWeightedEdge edge = graph.getEdge(vertex1, vertex2);
                    if (weight > 0.0) {
                        graph.setEdgeWeight(edge, weight);
                    } else {
                        graph.removeEdge(edge);
                    }
                }
            }
        }
    }

    public double calcSimilarity(SentenceVertex vertex1, SentenceVertex vertex2) {
        String[] vertex1Tokens = vertex1.getWordTokens();
        String[] vertex2Tokens = vertex2.getWordTokens();
        double counter = 0;
        for (String vertex1Token : vertex1Tokens) {
            for (String vertex2Token : vertex2Tokens) {
                if (vertex1Token.equals(vertex2Token) && vertex1Token.length() > 1) {
                    counter++;
                }
            }
        }
        return counter / (Math.log(vertex1Tokens.length) + Math.log(vertex2Tokens.length));
    }

    public double calcScore(SentenceVertex vertex) {
        double sum = 0;
        double DAMPING_FACTOR = 0.85;
        double score = 1 - DAMPING_FACTOR;
        for (DefaultWeightedEdge edge : graph.edgesOf(vertex)) {
            double numerator = graph.getEdgeWeight(edge);
            SentenceVertex vertex2 = graph.getEdgeTarget(edge);
            if (vertex == vertex2) {
                vertex2 = graph.getEdgeSource(edge);
            }
            double denominator = 0;
            for (DefaultWeightedEdge edge2 : graph.edgesOf(vertex2)) {
                denominator += graph.getEdgeWeight(edge2);
            }
            sum += (numerator / denominator) * vertex2.getScore();
        }
        score += DAMPING_FACTOR * sum;
        return score;
    }

    private void convergeScores() {
        double error = 1;
        int itr = 0;
        double THRESHOLD = 0.0001;
        while (error > THRESHOLD) {
            for (SentenceVertex vertex : graph.vertexSet()) {
                double newScore = calcScore(vertex);
                double lastScore = vertex.getScore();
                double scoreError = Math.abs(lastScore - newScore) / newScore;
                error += scoreError;
                vertex.setScore(newScore);
            }
            error = error / (double) (graph.vertexSet().size());
            itr++;
        }
        String TAG = "INFO";
        Log.i(TAG, "convergeScores: " + "\n** ITERATIONS ** - " + itr);
    }

    public ArrayList<SentenceVertex> sortSentences(String[] sentences, String[][] tokens) {
        makeGraph(sentences, tokens);
        convergeScores();
        ArrayList<SentenceVertex> sorted = new ArrayList<>(graph.vertexSet());
        Collections.sort(sorted, new SentenceComparator());
        return sorted;
    }

    public static class SentenceComparator implements Comparator<SentenceVertex> {
        @Override
        public int compare(SentenceVertex o1, SentenceVertex o2) {
            return Double.compare(o2.getScore(), o1.getScore());
        }
    }

    public static class SentenceVertex {
        String sentence;
        String[] wordTokens;
        double sentenceScore;

        public SentenceVertex(String aSentence, String[] words) {
            sentence = aSentence;
            wordTokens = words;
            sentenceScore = 1.0;
        }

        public String getSentence() {
            return sentence;
        }

        public String[] getWordTokens() {
            return wordTokens;
        }

        public double getScore() {
            return sentenceScore;
        }

        public void setScore(double sentenceScore) {
            this.sentenceScore = sentenceScore;
        }
    }
}
