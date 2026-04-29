package com.example.springai;

import com.example.springai.controller.ChatController;
import com.example.springai.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void returnsAssistantReply() throws Exception {
        given(chatService.chat(anyString())).willReturn("Hello from ChatGPT");

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":"Say hello"}
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.answer").value("Hello from ChatGPT"));
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":" "}
                        """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void mapsOpenAiQuotaErrorsToTooManyRequests() throws Exception {
        given(chatService.chat(anyString()))
            .willThrow(new NonTransientAiException("HTTP 429 - insufficient_quota"));

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":"Hello"}
                        """))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value("openai_quota_exceeded"));
    }

    @Test
    void mapsTransientAiErrorsToBadGateway() throws Exception {
        given(chatService.chat(anyString()))
            .willThrow(new TransientAiException("temporary upstream error"));

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":"Hello"}
                        """))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value("ai_service_unavailable"));
    }
}
