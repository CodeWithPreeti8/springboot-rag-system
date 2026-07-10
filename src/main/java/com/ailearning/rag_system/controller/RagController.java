package com.ailearning.rag_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ailearning.rag_system.dto.RagRequest;
import com.ailearning.rag_system.dto.RagResponse;
import com.ailearning.rag_system.service.RagService;

@RestController
@RequestMapping("/api/rag")
public class RagController {
	
	@Autowired
    private RagService ragService;
	
	@GetMapping("/health")
    public String health() {
        System.out.println("\n[HEALTH CHECK] Request received");
        System.out.println("[HEALTH CHECK] ✓ Application is running!\n");
        return "RAG System is running!";
    }
	
	@PostMapping("/ask")
    public RagResponse ask(@RequestBody RagRequest request) {
        
        String question = request.getQuestion();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RAG CONTROLLER: /api/rag/ask endpoint called");
        System.out.println("=".repeat(70));
        System.out.println("Received question: " + question);
        System.out.println("=".repeat(70));
        
        // Call RagService to process the question
        System.out.println("\n[CONTROLLER] Calling RagService.processQuestion()...");
        RagResponse response = ragService.processQuestion(request);
        System.out.println("[CONTROLLER] ✓ Response received from RagService");
        
        // Print response details
        System.out.println("\n[CONTROLLER] Response details:");
        System.out.println("  Question: " + response.getQuestion());
        System.out.println("  Source: " + response.getSourceDocument());
        System.out.println("  Answer preview: " + 
                          response.getAnswer().substring(0, Math.min(50, response.getAnswer().length())) + "...");
        System.out.println("  Timestamp: " + response.getTimeStamp());
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RAG CONTROLLER: Returning response to user");
        System.out.println("=".repeat(70) + "\n");
        
        return response;
    }

}
