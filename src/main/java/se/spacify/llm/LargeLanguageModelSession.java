package se.spacify.llm;

public class LargeLanguageModelSession {
    private final LargeLanguageModel model;

    public LargeLanguageModelSession(LargeLanguageModel model) {
        this.model = model;
    }

    public LargeLanguageModel getModel() {
        return model;
    }
    
}
