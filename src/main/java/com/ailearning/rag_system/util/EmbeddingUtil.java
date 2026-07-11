package com.ailearning.rag_system.util;

import com.google.gson.Gson;
import java.util.Arrays;

public class EmbeddingUtil {
    
    private static final Gson gson = new Gson();
    
    
    // ================================================
    // FUNCTION 1: generateSimpleEmbedding()
    // CALLED FIRST when user asks question
    // ================================================
    
    /**
     * Convert user question to embedding (numbers)
     * 
     * INPUT: "What is Java?"
     * OUTPUT: [0.05, 0.1, 0.08, 0.0, ...] (384 dimensions)
     * 
     * WHEN CALLED: Every time user asks a question
     */
    public static double[] generateSimpleEmbedding(String text) {
        System.out.println("[STEP 1] Generating embedding for: " + text);
        
        // Create 384-dimensional embedding
        double[] embedding = new double[384];
        
        // Convert to lowercase
        String normalized = text.toLowerCase();
        
        // Split into words
        String[] words = normalized.split("\\W+");
        System.out.println("  Words found: " + Arrays.toString(words));
        
        // For each word, increase embedding at specific dimension
        for (String word : words) {
            if (word.length() > 0) {
                // Hash word to get dimension (0-54, not 0-99)
                int index = Math.abs(word.hashCode()) % 384;
                embedding[index] += 0.1;
                System.out.println("  Word '" + word + "' → dimension " + index);
            }
        }
        
        // Normalize (make values 0-1)
        double sum = Arrays.stream(embedding).sum();
        if (sum > 0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= sum;
            }
        }
        
        System.out.println("  ✓ Question embedding generated!");
        return embedding;
    }
    
    // ================================================
    // FUNCTION 2: parseEmbedding()
    // CALLED SECOND to convert stored embeddings
    // ================================================
    
    /**
     * Parse embedding from database (JSON text to Java numbers)
     * 
     * INPUT: "[0.9, 0.0, 0.8, 0.0, ...]"  (TEXT from database)
     * OUTPUT: [0.9, 0.0, 0.8, 0.0, ...]   (Java double array)
     * 
     * WHEN CALLED: For each document in database
     */
    public static double[] parseEmbedding(String embeddingJson) {
        try {
            if (embeddingJson == null || embeddingJson.isEmpty()) {
                return new double[0];
            }
            
            // Convert JSON string to Double array
            Double[] array = gson.fromJson(embeddingJson, Double[].class);
            
            // Convert Double[] to double[]
            double[] result = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i] != null ? array[i] : 0.0;
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Error parsing embedding: " + e.getMessage());
            return new double[0];
        }
    }
    
    
    // ================================================
    // FUNCTION 3: calculateDistance()
    // CALLED THIRD to find similarity
    // ================================================
    
    /**
     * Calculate Euclidean distance between two embeddings
     * 
     * SMALLER distance = MORE similar
     * 
     * INPUT: 
     *   vec1 = [0.05, 0.1, 0.08, ...]  (question embedding)
     *   vec2 = [0.9, 0.0, 0.8, ...]    (document embedding)
     * OUTPUT: 0.15 (distance score)
     * 
     * FORMULA: √[(a₁-b₁)² + (a₂-b₂)² + ... + (aₙ-bₙ)²]
     * 
     * WHEN CALLED: For each document to compare with question
     */
    public static double calculateDistance(double[] vec1, double[] vec2) {
        
        // Check same length
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must have same length");
        }
        
        double sum = 0;
        
        // Calculate sum of squared differences
        for (int i = 0; i < vec1.length; i++) {
            double diff = vec1[i] - vec2[i];
            sum += diff * diff;
        }
        
        // Return square root
        return Math.sqrt(sum);
    }
    
    
    // ================================================
    // FUNCTION 4: embeddingToJson()
    // CALLED FOURTH if you need to store/send
    // ================================================
    
    /**
     * Convert embedding array back to JSON string
     * 
     * INPUT: [0.9, 0.0, 0.8, 0.0, ...]
     * OUTPUT: "[0.9, 0.0, 0.8, 0.0, ...]"
     * 
     * WHEN CALLED: When storing embedding in database
     */
    public static String embeddingToJson(double[] embedding) {
        return gson.toJson(embedding);
    }
}