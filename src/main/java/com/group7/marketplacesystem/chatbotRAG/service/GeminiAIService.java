package com.group7.marketplacesystem.chatbotRAG.service;

/**
 * Service interface for Google Gemini AI operations.
 * Provides methods for generating embeddings and chat responses using Google's
 * Gemini API.
 */
public interface GeminiAIService {

    /**
     * Generate vector embedding for given text using Gemini text-embedding-004.
     * 
     * @param text Input text to embed
     * @return Embedding vector as double array (dimension 768)
     */
    double[] generateEmbedding(String text);

    /**
     * Generate chat response using Gemini with contextual information (for RAG).
     * 
     * @param userMessage User's message
     * @param context     Retrieved context from vector search
     * @return AI response text
     */
    String generateContextualResponse(String userMessage, String context);

    /**
     * Generate mock embedding (random but consistent for same text).
     * Used when GEMINI_MOCK_MODE=true
     * 
     * @param text Input text
     * @return Mock embedding vector
     */
    double[] generateMockEmbedding(String text);

    /**
     * Generate mock contextual response.
     * Used when GEMINI_MOCK_MODE=true
     * 
     * @param userMessage User's message
     * @param context     Retrieved context
     * @return Mock response text
     */
    String generateMockContextualResponse(String userMessage, String context);
}
