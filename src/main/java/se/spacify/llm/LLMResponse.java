package se.spacify.llm;

public class LLMResponse {
    private final String responseText;

    public LLMResponse(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }
    
}
