package com.msa.ai_service.client;


import com.msa.ai_service.dto.AiRequest;
import com.msa.ai_service.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AiClient {
    private final RestClient restClient;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    public AiResponse generate(String prompt) {
        AiRequest request = AiRequest.of(prompt);

        return restClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .body(request)
                .retrieve()
                .body(AiResponse.class);
    }
}
