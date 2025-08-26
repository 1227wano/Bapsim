package com.bapsim.entity;

import javax.persistence.*;

@Entity
@Table(name = "AI_Service")
public class AIService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SESSION_ID")
    private Integer sessionId;
    
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;
    
    @Column(name = "USER_MESSAGE", length = 500, nullable = false)
    private String userMessage;
    
    @Column(name = "ASSISTANT_MESSAGE", length = 500)
    private String assistantMessage;
    
    @Column(name = "USAGE_PROMPT_TOKEN")
    private Integer usagePromptToken;
    
    @Column(name = "USAGE_COMPLETION_TOKEN")
    private Integer usageCompletionToken;
    
    @Column(name = "USAGE_TOTAL_TOKENS")
    private Integer usageTotalTokens;
    
    @Column(name = "LLM_LATENCY_MS")
    private Integer llmLatencyMs;
    
    @Column(name = "TOTAL_LATENCY_MS")
    private Integer totalLatencyMs;
    
    @Column(name = "RAG_USED")
    private Boolean ragUsed;
    
    @Column(name = "LANGUAGE", length = 5)
    private String language;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", insertable = false, updatable = false)
    private Member user;
    
    // Constructors
    public AIService() {}
    
    // Getters and Setters
    public Integer getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getUserNo() {
        return userNo;
    }
    
    public void setUserNo(Long userNo) {
        this.userNo = userNo;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
    
    public String getAssistantMessage() {
        return assistantMessage;
    }
    
    public void setAssistantMessage(String assistantMessage) {
        this.assistantMessage = assistantMessage;
    }
    
    public Integer getUsagePromptToken() {
        return usagePromptToken;
    }
    
    public void setUsagePromptToken(Integer usagePromptToken) {
        this.usagePromptToken = usagePromptToken;
    }
    
    public Integer getUsageCompletionToken() {
        return usageCompletionToken;
    }
    
    public void setUsageCompletionToken(Integer usageCompletionToken) {
        this.usageCompletionToken = usageCompletionToken;
    }
    
    public Integer getUsageTotalTokens() {
        return usageTotalTokens;
    }
    
    public void setUsageTotalTokens(Integer usageTotalTokens) {
        this.usageTotalTokens = usageTotalTokens;
    }
    
    public Integer getLlmLatencyMs() {
        return llmLatencyMs;
    }
    
    public void setLlmLatencyMs(Integer llmLatencyMs) {
        this.llmLatencyMs = llmLatencyMs;
    }
    
    public Integer getTotalLatencyMs() {
        return totalLatencyMs;
    }
    
    public void setTotalLatencyMs(Integer totalLatencyMs) {
        this.totalLatencyMs = totalLatencyMs;
    }
    
    public Boolean getRagUsed() {
        return ragUsed;
    }
    
    public void setRagUsed(Boolean ragUsed) {
        this.ragUsed = ragUsed;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Member getUser() {
        return user;
    }
    
    public void setUser(Member user) {
        this.user = user;
    }
}
