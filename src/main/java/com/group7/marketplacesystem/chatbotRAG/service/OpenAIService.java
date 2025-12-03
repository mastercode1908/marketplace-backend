package com.group7.marketplacesystem.chatbotRAG.service;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public interface OpenAIService {
    double[] generateEmbedding(String text);
    String generateChatResponse(List<ChatMessage> messages);
    String generateContextualResponse(String userMessage, String context);
    double[] generateMockEmbedding(String text);
    String generateMockResponse(List<ChatMessage> messages);
    String generateMockContextualResponse(String userMessage, String context);
}
