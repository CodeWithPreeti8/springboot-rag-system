package com.ailearning.rag_system.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordService {
    
    // Common words to ignore
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "is", "are", "was", "were", "be", "been", "being", "have", "has",
        "had", "do", "does", "did", "will", "would", "should", "could", "may",
        "might", "can", "this", "that", "these", "those", "i", "you", "he",
        "she", "it", "we", "they", "what", "which", "who", "when", "where",
        "why", "how", "all", "each", "every", "both", "few", "more", "most",
        "other", "some", "such", "no", "nor", "not", "only", "same", "so",
        "than", "too", "very", "just", "as", "if", "also", "from", "by",
        "with", "about", "into", "through", "during", "before", "after",
        "above", "below", "up", "down", "out", "off", "over", "under",
        "again", "further", "then", "once"
    ));
    
    /**
     * Extract important keywords from text
     * Returns top keywords that aren't stop words
     */
    public List<String> extractKeywords(String text) {
        // Convert to lowercase and split
        String[] words = text.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")  // Remove special chars
            .split("\\s+");
        
        // Count word frequencies
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : words) {
            if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }
        
        // Sort by frequency and return top keywords
        return wordFreq.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .map(Map.Entry::getKey)
            .limit(10)  // Top 10 keywords
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate keyword match score
     * How many keywords from question match in document
     */
    public int calculateMatchScore(String question, String document) {
        List<String> questionKeywords = extractKeywords(question);
        String docLower = document.toLowerCase();
        
        int score = 0;
        for (String keyword : questionKeywords) {
            // Check if keyword exists in document
            if (containsWord(docLower, keyword)) {
                score += 10;  // Each match = 10 points
            }
        }
        
        return score;
    }
    
    /**
     * Helper: Check if word exists (not substring)
     */
    private boolean containsWord(String text, String word) {
        return text.matches(".*\\b" + word + "\\b.*");
    }
}