package com.figueredo.ricardo.mvp.assistente.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AssistenteAiProperties {

    private String provider = "ollama";
    private OllamaProperties ollama = new OllamaProperties();
    private GeminiProperties gemini = new GeminiProperties();

    @Getter
    @Setter
    public static class OllamaProperties {
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3.1";
        private int timeoutSeconds = 60;
    }

    @Getter
    @Setter
    public static class GeminiProperties {
        private String apiKey = "";
        private String model = "gemini-1.5-flash";
        private String baseUrl = "https://generativelanguage.googleapis.com";
        private int timeoutSeconds = 60;
    }
}
