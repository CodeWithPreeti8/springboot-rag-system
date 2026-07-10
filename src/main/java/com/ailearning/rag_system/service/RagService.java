package com.ailearning.rag_system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ailearning.rag_system.dto.RagRequest;
import com.ailearning.rag_system.dto.RagResponse;
import com.ailearning.rag_system.entity.Document;
import com.ailearning.rag_system.repository.DocumentRepository;
import com.ailearning.rag_system.util.EmbeddingUtil;

@Service
public class RagService {
	@Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private GroqService groqService;
    
    public RagResponse processQuestion(RagRequest request) {
        
        String question = request.getQuestion();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RAG SERVICE: Processing question");
        System.out.println("=".repeat(60));
        System.out.println("Question: " + question);
        System.out.println("=".repeat(60));
        
        
        // ================================================
        // STEP 1: Generate embedding for question
        // ================================================
        System.out.println("\n[STEP 1] Generating question embedding...");
        double[] questionEmbedding = EmbeddingUtil.generateSimpleEmbedding(question);
        System.out.println("[STEP 1] ✓ Question embedding generated!");
        System.out.println("         Embedding dimensions: " + questionEmbedding.length);
        System.out.println("         First 5 values: [" + 
                          String.format("%.3f", questionEmbedding[0]) + ", " +
                          String.format("%.3f", questionEmbedding[1]) + ", " +
                          String.format("%.3f", questionEmbedding[2]) + ", " +
                          String.format("%.3f", questionEmbedding[3]) + ", " +
                          String.format("%.3f", questionEmbedding[4]) + ", ...]");
        
        
        // ================================================
        // STEP 2: Get all documents from database
        // ================================================
        System.out.println("\n[STEP 2] Fetching documents from database...");
        List<Document> allDocuments = documentRepository.findAll();
        System.out.println("[STEP 2] ✓ Documents fetched!");
        System.out.println("         Total documents: " + allDocuments.size());
        
        
        // ================================================
        // STEP 3: Find most similar document
        // ================================================
        System.out.println("\n[STEP 3] Calculating distances and finding similarity...");
        
        Document mostSimilarDoc = null;
        double smallestDistance = Double.MAX_VALUE;
        
        // Loop through each document
        for (Document doc : allDocuments) {
            
            // Parse embedding from database (TEXT to numbers)
            double[] docEmbedding = EmbeddingUtil.parseEmbedding(doc.getEmbedding());
            
            // Only process if embedding exists and has values
            if (docEmbedding.length > 0) {
                
                // Calculate distance between question and document
                double distance = EmbeddingUtil.calculateDistance(
                    questionEmbedding, 
                    docEmbedding
                );
                
                // Print result for this document
                System.out.println("  Document ID " + doc.getId() + 
                                 " (\"" + doc.getTitle() + "\"): distance = " + 
                                 String.format("%.4f", distance));
                
                // Keep track of smallest distance (most similar)
                if (distance < smallestDistance) {
                    smallestDistance = distance;
                    mostSimilarDoc = doc;
                }
            }
        }
        
        System.out.println("[STEP 3] ✓ Distance calculation complete!");
        
        
        // ================================================
        // STEP 4: Handle case where no documents found
        // ================================================
        if (mostSimilarDoc == null) {
            System.out.println("\n[WARNING] No relevant documents found!");
            
            return new RagResponse(
                question,
                "No relevant documents found in the database",
                "N/A",
                null,
                System.currentTimeMillis()
            );
        }
     // ================================================
        // STEP 5: Print most similar document
        // ================================================
        System.out.println("\n[STEP 4] Most similar document found!");
        System.out.println("         ID: " + mostSimilarDoc.getId());
        System.out.println("         Title: " + mostSimilarDoc.getTitle());
        System.out.println("         Distance: " + String.format("%.4f", smallestDistance));
        System.out.println("         Content length: " + mostSimilarDoc.getContent().length() + " chars");
        
        
        // ================================================
        // STEP 6: Send to Groq/ChatGPT
        // ================================================
        System.out.println("\n[STEP 5] Sending to Groq API for answer generation...");
        String answer = groqService.generateAnswer(question, mostSimilarDoc.getContent());
        System.out.println("[STEP 5] ✓ Answer generated!");
        System.out.println("         Answer length: " + answer.length() + " chars");
        
        
        // ================================================
        // STEP 7: Build and return response
        // ================================================
        System.out.println("\n[STEP 6] Building response...");
        
        RagResponse response = new RagResponse(
            question,
            answer,
            mostSimilarDoc.getTitle(),
            mostSimilarDoc.getId(),
            System.currentTimeMillis()
        );
        
        System.out.println("[STEP 6] ✓ Response built!");
        System.out.println("=".repeat(60));
        System.out.println("RAG SERVICE: Processing complete!");
        System.out.println("=".repeat(60) + "\n");
        
        return response;
    }


}
