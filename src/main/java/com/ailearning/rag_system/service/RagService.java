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
    
    @Autowired
    private KeywordService keywordService;  // Add this!

    public RagResponse processQuestion(RagRequest request) {
        
        String question = request.getQuestion();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RAG SERVICE: Processing question");
        System.out.println("=".repeat(60));
        System.out.println("Question: " + question);
        System.out.println("=".repeat(60));
        
        // STEP 1: Get all documents
        System.out.println("\n[STEP 1] Fetching documents from database...");
        List<Document> allDocuments = documentRepository.findAll();
        System.out.println("[STEP 1] ✓ Found " + allDocuments.size() + " documents");
        
        // STEP 2: Extract keywords from question
        System.out.println("\n[STEP 2] Extracting keywords from question...");
        List<String> questionKeywords = keywordService.extractKeywords(question);
        System.out.println("[STEP 2] ✓ Keywords: " + questionKeywords);
        
        // STEP 3: Find best matching document
        System.out.println("\n[STEP 3] Finding best matching document...");
        
        Document bestDocument = null;
        int maxScore = 0;
        
        for (Document doc : allDocuments) {
            // Calculate match score dynamically!
            int score = keywordService.calculateMatchScore(question, doc.getContent());
            
            System.out.println("  Document " + doc.getId() + 
                             " (\"" + doc.getTitle() + "\"): " + score + " points");
            
            if (score > maxScore) {
                maxScore = score;
                bestDocument = doc;
            }
        }
        
        // Handle no match
        if (bestDocument == null || maxScore == 0) {
            System.out.println("\n[WARNING] No matching document found!");
            return new RagResponse(
                question,
                "No relevant documents found in database",
                "N/A",
                null,
                System.currentTimeMillis()
            );
        }
        
        System.out.println("\n[STEP 4] Best document selected!");
        System.out.println("  ID: " + bestDocument.getId());
        System.out.println("  Title: " + bestDocument.getTitle());
        System.out.println("  Match score: " + maxScore);
        
        // STEP 5: Send to Groq
        System.out.println("\n[STEP 5] Sending to Groq API...");
        String answer = groqService.generateAnswer(question, bestDocument.getContent());
        System.out.println("[STEP 5] ✓ Answer generated!");
        
        // STEP 6: Build response
        System.out.println("\n[STEP 6] Building response...");
        RagResponse response = new RagResponse(
            question,
            answer,
            bestDocument.getTitle(),
            bestDocument.getId(),
            System.currentTimeMillis()
        );
        
        System.out.println("=".repeat(60));
        System.out.println("RAG SERVICE: Complete!");
        System.out.println("=".repeat(60) + "\n");
        
        return response;
    }
}
