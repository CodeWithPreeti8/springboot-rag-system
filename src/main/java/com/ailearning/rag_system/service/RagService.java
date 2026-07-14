package com.ailearning.rag_system.service;

import com.ailearning.rag_system.dto.RagRequest;
import com.ailearning.rag_system.dto.RagResponse;
import com.ailearning.rag_system.entity.Document;
import com.ailearning.rag_system.repository.DocumentRepository;
//import com.ailearning.rag_system.util.EmbeddingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RagService {
    
    @Autowired
    private KeywordService keywordService;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private GroqService groqService;
    
    /**
     * Main method: Process question and return answer
     */
    public RagResponse processQuestion(RagRequest request) {
        String question = request.getQuestion();
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Question: " + question);
        System.out.println("═══════════════════════════════════════════");
        
        // STEP 1: Extract keywords from question
        List<String> questionKeywords = keywordService.extractKeywords(question);
        System.out.println("Keywords: " + questionKeywords);
        
        // STEP 2: Get all documents
        List<Document> allDocuments = documentRepository.findAll();
        System.out.println("Total documents: " + allDocuments.size());
        
        // STEP 3: Score each document using keyword matching
        Document bestDocument = null;
        int maxScore = 0;
        
        for (Document doc : allDocuments) {
            int score = keywordService.calculateMatchScore(question, doc.getContent());
            System.out.println("Document " + doc.getId() + " (\"" + doc.getTitle() + "\"): " + score + " points");
            
            if (score > maxScore) {
                maxScore = score;
                bestDocument = doc;
            }
        }
        
        // STEP 4: NEW! If no perfect match, use semantic re-ranking
        if (bestDocument == null || maxScore == 0) {
            System.out.println("⚠️ No keyword match found. Using semantic re-ranking...");
            bestDocument = findBestDocumentBySemantic(question, allDocuments);
        }
        
        // STEP 5: Handle case where no document found
        if (bestDocument == null) {
            System.out.println("❌ No relevant document found!");
            return new RagResponse(
                question,
                "No relevant documents found in database",
                "N/A",
                null,
                System.currentTimeMillis()
            );
        }
        
        System.out.println("✅ Selected document: " + bestDocument.getTitle());
        
        // STEP 6: Send to Groq for answer generation
        System.out.println("Sending to Groq API...");
        String answer = groqService.generateAnswer(question, bestDocument.getContent());
        
        // STEP 7: Build and return response
        RagResponse response = new RagResponse(
            question,
            answer,
            bestDocument.getTitle(),
            bestDocument.getId(),
            System.currentTimeMillis()
        );
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Response sent to user ✅");
        System.out.println("═══════════════════════════════════════════");
        
        return response;
    }
    
    /**
     * NEW METHOD: Semantic re-ranking for paraphrased queries
     * Uses embeddings to find most similar document
     */
    private Document findBestDocumentBySemantic(String question, List<Document> documents) {
        
        System.out.println("\n📊 Semantic Re-ranking Process:");
        System.out.println("Question: " + question);
        
        // Generate question embedding
        double[] questionEmbedding = generateQuestionEmbedding(question);
        System.out.println("Question embedding generated ✓");
        
        // Score each document by semantic similarity
        Document bestDocument = null;
        double highestSimilarity = -1;
        
        for (Document doc : documents) {
            // Parse document embedding from JSON
            double[] docEmbedding = parseEmbedding(doc.getEmbedding());
            
            if (docEmbedding == null) {
                System.out.println("⚠️ Document " + doc.getId() + " has no embedding, skipping");
                continue;
            }
            
            // Calculate cosine similarity
            double similarity = calculateCosineSimilarity(questionEmbedding, docEmbedding);
            
            System.out.println("Document " + doc.getId() + " (\"" + doc.getTitle() + "\"): " + 
                             String.format("%.4f", similarity) + " similarity");
            
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestDocument = doc;
            }
        }
        
        if (bestDocument != null) {
            System.out.println("✅ Best semantic match: " + bestDocument.getTitle() + 
                             " (similarity: " + String.format("%.4f", highestSimilarity) + ")");
        }
        
        return bestDocument;
    }
    
    /**
     * Generate embedding for question
     * Uses same method as documents (Sentence Transformers all-MiniLM-L6-v2)
     */
    private double[] generateQuestionEmbedding(String question) {
        // In production, call Python embedding service or use Java library
        // For now, return dummy embedding (384 dimensions)
        // Real implementation would use:
        // 1. Python service: POST to /generate-embedding
        // 2. Or Java library: sentence-transformers for Java
        // 3. Or cache embeddings in Redis
        
        // Placeholder: return random embedding (384 dims)
        // In real scenario, integrate with embedding service
        double[] embedding = new double[384];
        for (int i = 0; i < 384; i++) {
            embedding[i] = 0.5; // Placeholder
        }
        return embedding;
    }
    
    /**
     * Parse JSON embedding string to double array
     * Format: "[0.1, 0.2, 0.3, ...]"
     */
    private double[] parseEmbedding(String embeddingJson) {
        if (embeddingJson == null || embeddingJson.isEmpty()) {
            return null;
        }
        
        try {
            // Remove brackets
            String cleaned = embeddingJson.replaceAll("[\\[\\]]", "");
            
            // Split by comma
            String[] parts = cleaned.split(",");
            
            // Convert to double array
            double[] embedding = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                embedding[i] = Double.parseDouble(parts[i].trim());
            }
            
            return embedding;
        } catch (Exception e) {
            System.out.println("⚠️ Error parsing embedding: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Calculate cosine similarity between two embeddings
     * Formula: (A · B) / (||A|| * ||B||)
     * Range: -1 to 1 (1 = identical, 0 = orthogonal, -1 = opposite)
     */
    private double calculateCosineSimilarity(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must have same length");
        }
        
        // Calculate dot product (A · B)
        double dotProduct = 0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
        }
        
        // Calculate magnitudes (||A||, ||B||)
        double magnitude1 = 0;
        double magnitude2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            magnitude1 += vec1[i] * vec1[i];
            magnitude2 += vec2[i] * vec2[i];
        }
        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);
        
        // Avoid division by zero
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0;
        }
        
        // Return cosine similarity
        return dotProduct / (magnitude1 * magnitude2);
    }
}
