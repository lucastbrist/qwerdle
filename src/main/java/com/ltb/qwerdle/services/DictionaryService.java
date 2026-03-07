package com.ltb.qwerdle.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ltb.qwerdle.exceptions.DictionaryServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.ltb.qwerdle.utils.WordValidator.*;

@Slf4j
@Service
public class DictionaryService {

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private static final double frequencyVar = 4.50;

    public DictionaryService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Value("${dictionary.api.key}")
    private String apiKey;

    @Value("${dictionary.base-url}")
    private String baseUrl;

    @Value("${dictionary.host}")
    private String hostHeader;

    private HttpEntity<Void> createRequestEntity() {
        // RapidAPI requires these headers for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", hostHeader);
        headers.set("x-rapidapi-key", apiKey);
        return new HttpEntity<>(headers);
    }

    /**
     * Fetches a random word of the specified length from the dictionary API.
     *
     * @param length length of the desired word; must be between 1 and 15.
     * @return a random word of the specified length.
     * @throws IllegalArgumentException       if length is invalid or no word is available.
     * @throws DictionaryServiceException     if there is an error communicating with the dictionary API.
     */

    @SuppressWarnings("BusyWait")
    public String getRandomWord(int length) {

        int attempts = 0;
        int maxAttempts = 50;

        if (length <= 0 || length > 15) {
            throw new IllegalArgumentException("Length must be a positive integer no greater than 15.");
        }

        try {

            HttpEntity<Void> requestEntity = createRequestEntity();
            String word = "";

            do {

                ResponseEntity<String> response = restTemplate.exchange(
                        baseUrl + "/words/?letters=" + length + "&random=true",
                        HttpMethod.GET, requestEntity, String.class);

                if (!response.hasBody() || response.getStatusCode().is4xxClientError()) {
                    throw new IllegalArgumentException("No words of length " + length + " available");
                }

                String responseBody = response.getBody();

                try {
                    word = extractWordFromJson(responseBody);
                    word = normalizeWord(word);
                } catch (IllegalArgumentException e) {
                    // Bad word (non-alphabetic, empty, null, etc.); skip and try again
                    attempts ++;
                    continue;
                }

                // Small delay to avoid rate limiting
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                attempts++;

                // NOTE: isFrequentWord() can cause excessive loops and expensive API calls;
                // this is not for a production environment
            } while (attempts < maxAttempts &&
                    (!isValidAlphabeticWord(word) || !isFrequentWord(word) || word.length() != 5));

            if (attempts >= maxAttempts) {
                throw new DictionaryServiceException(
                        "Failed to fetch a valid word after " + maxAttempts + " attempts");
            }

            return word;

        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("No words of length " + length + " available", e);

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new DictionaryServiceException("Dictionary API authentication failed - check API key", e);

        } catch (HttpClientErrorException e) {
            throw new DictionaryServiceException("Dictionary API client error: " + e.getStatusCode(), e);

        } catch (HttpServerErrorException e) {
            throw new DictionaryServiceException("Dictionary API server error: " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {
            throw new DictionaryServiceException("Cannot reach dictionary API", e);

        } catch (JsonProcessingException e) {
            throw new DictionaryServiceException("Failed to parse JSON response from dictionary API", e);

        } catch (RestClientException e) {
            throw new DictionaryServiceException("Failed to fetch random word from dictionary API", e);
        }
    }

    boolean isValidDictionaryWord(String guess) {

        /*
         Method to validate player guess.
         Searches dictionary API for the guessed word.
         Returns boolean based on API response.
         Throws various exceptions on failure states.
        */

        if (guess == null || guess.isEmpty()) {
            throw new IllegalArgumentException("Word passed to dictionary validation cannot be null or empty");
        }

        try {
            HttpEntity<Void> requestEntity = createRequestEntity();
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/words/" + guess,
                    HttpMethod.GET, requestEntity, String.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (HttpClientErrorException.NotFound e) {
            // 404 = word doesn't exist - this is expected, not an error
            return false;
        }

        // Failure states
        catch (HttpClientErrorException.Unauthorized e) {
            throw new DictionaryServiceException("Dictionary API authentication failed", e);

        } catch (HttpClientErrorException e) {
            throw new DictionaryServiceException("Dictionary API client error: " + e.getStatusCode(), e);

        } catch (HttpServerErrorException e) {
            throw new DictionaryServiceException("Dictionary API server error: " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {
            throw new DictionaryServiceException("Cannot reach dictionary API", e);

        } catch (RestClientException e) {
            throw new DictionaryServiceException("Failed to validate word", e);
        }

    }

    boolean isFrequentWord(String word) {

        /*
         Method to check the frequency of an API-returned word.
         Calls API again with the returned word.
         Returns boolean based on json node filtering.
         Throws various exceptions on failure states.

         NOTE: EXTREMELY EXPENSIVE API CALLS
        */

        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word passed to frequency validation cannot be null or empty");
        }

        try {

            HttpEntity<Void> requestEntity = createRequestEntity();
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/words/" + word + "/frequency",
                    HttpMethod.GET, requestEntity, String.class);

            String responseBody = response.getBody();
            double frequency = extractFrequencyFromJson(responseBody);

            return frequency >= frequencyVar;

        } catch (HttpClientErrorException.NotFound e) {
            // 404 = word doesn't exist - this IS an error, unlike in isValidDictionaryWord()
            // as this should be from the API originally
            throw new DictionaryServiceException("Dictionary API frequency validation failed", e);
        }

        catch (HttpClientErrorException.Unauthorized e) {
            throw new DictionaryServiceException("Dictionary API authentication failed", e);

        } catch (HttpClientErrorException e) {
            throw new DictionaryServiceException("Dictionary API client error: " + e.getStatusCode(), e);

        } catch (HttpServerErrorException e) {
            throw new DictionaryServiceException("Dictionary API server error: " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {
            throw new DictionaryServiceException("Cannot reach dictionary API", e);

        } catch (JsonProcessingException e) {
            throw new DictionaryServiceException("Failed to parse JSON response from dictionary API", e);

        } catch (RestClientException e) {
            throw new DictionaryServiceException("Failed to validate word", e);
        }

    }

    private String extractWordFromJson(String jsonResponse) throws JsonProcessingException {

        /*
        Extracts just the "word" field from JSON response to avoid unnecessary object mapping.
         */

        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode wordNode = root.get("word");
        return wordNode != null ? wordNode.asText() : null;
    }

    private double extractFrequencyFromJson(String jsonResponse) throws JsonProcessingException {

        /*
         * Extracts just the "zipf" frequency field from JSON response to avoid unnecessary object mapping.
         */

        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode zipfNode = root.path("frequency").path("zipf");

        if (zipfNode.isMissingNode() || zipfNode.isNull()) {
            // Treat missing or null as 0, will fail the >= frequencyVar check in isFrequentWord()
            // WordsAPI does not always have this field present in the JSON response,
            // but we want to handle gracefully and loop
            return 0.0;
        }

        return zipfNode.asDouble();
    }

}
