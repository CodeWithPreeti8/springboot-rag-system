package com.ailearning.rag_system.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class GroqService {
	@Value("${groq.api.key}")
    private String apiKey;
    
    @Value("${groq.api.url}")
    private String apiUrl;
    
    @Value("${groq.model}")
    private String model;
	
    private static final Gson gson = new Gson();
    
    // ================================================
    // FUNCTION 1: generateAnswer()
    // Main function - called by RagService
    // ================================================
    
    /**
     * FLOW:
     * 1. Build prompt with context
     * 2. Call Groq API
     * 3. Parse response
     * 4. Return answer
     */
    public String generateAnswer(String question, String documentContext) {
        try {
            System.out.println("\n[GROQ] Starting answer generation...");
            System.out.println("[GROQ] Question: " + question);
            System.out.println("[GROQ] Document context length: " + documentContext.length() + " chars");
            
            // Step 1: Build prompt with document context
            String prompt = buildPrompt(question, documentContext);
            System.out.println("[GROQ] Prompt built. Length: " + prompt.length() + " chars");
            
            // Step 2: Call Groq API
            String answer = callGroqApi(prompt);
            System.out.println("[GROQ] ✓ Answer generated!");
            
            return answer;
            
        } catch (Exception e) {
            System.err.println("[GROQ] Error: " + e.getMessage());
            e.printStackTrace();
            return "Error generating response: " + e.getMessage();
        }
    }
      
    
 // ================================================
    // FUNCTION 2: buildPrompt()
    // Create prompt with document context
    // ================================================
    
    private String buildPrompt(String question, String documentContext) {
        return "Based on the following document:\n\n" +
               documentContext + "\n\n" +
               "Answer this question:\n" + question + "\n\n" +
               "Provide a clear and concise answer based ONLY on the document above.";
    }
    
 // ================================================
    // FUNCTION 3: callGroqApi()
    // Make HTTP request to Groq API
    // ================================================
    
    /**
     * Call Groq API via HTTP POST request
     */
    private String callGroqApi(String prompt) throws Exception {
        System.out.println("[HTTP] Creating connection to Groq API...");
        System.out.println("[HTTP] URL: " + apiUrl + "/chat/completions");
        
        // STEP 1: Create URL connection
        URL url = new URL(apiUrl + "/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        // STEP 2: Set HTTP method and headers
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        System.out.println("[HTTP] Headers set. API Key: " + apiKey.substring(0, 10) + "...");
        
        // STEP 3: Build request body as JSON
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("temperature", 0.7);  // Creativity level (0-1)
        body.addProperty("max_tokens", 500);   // Max response length
        
        // Create message object
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        
        // Add message to messages array
        JsonArray messages = new JsonArray();
        messages.add(message);
        body.add("messages", messages);
        
        System.out.println("[HTTP] Request body built with model: " + model);
        
        // STEP 4: Send request to Groq
        String jsonBody = gson.toJson(body);
        System.out.println("[HTTP] Sending request to Groq...");
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // STEP 5: Get response code
        int responseCode = conn.getResponseCode();
        System.out.println("[HTTP] Response code: " + responseCode);
        
        // STEP 6: Read response
        if (responseCode == 200) {
            // Success! Read the response
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            System.out.println("[HTTP] ✓ Response received!");
            
            // STEP 7: Parse JSON response
            String responseBody = response.toString();
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            
            // Extract answer from nested JSON
            // Response format: {"choices": [{"message": {"content": "..."}}]}
            String answer = responseJson
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
            
            System.out.println("[HTTP] Answer extracted: " + answer.substring(0, Math.min(50, answer.length())) + "...");
            
            // STEP 8: Return answer
            return answer;
            
        } else {
            // Error! Read error response
            System.err.println("[HTTP] ✗ Error response from Groq!");
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream())
            );
            StringBuilder errorResponse = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                errorResponse.append(line);
            }
            
            System.err.println("[HTTP] Error body: " + errorResponse.toString());
            return "Error: " + responseCode + " - " + errorResponse.toString();
        }
    }

}
