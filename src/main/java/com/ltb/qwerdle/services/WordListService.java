package com.ltb.qwerdle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class WordListService {

    private final List<String> commonWords;
    private final Random random = new Random();
    private static final String FALLBACK_WORD = "ADIEU";

    public WordListService(List<String> commonWords) {
        this.commonWords = loadWordList();
    }

    private List<String> loadWordList() {
        List<String> words = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource("word-list.txt");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (line.length() == 5 && line.matches("[A-Z]+")) {
                    words.add(line);
                }
            }
            reader.close();

            log.info("Loaded {} common 5-letter words", words.size());

        } catch (IOException e) {
            log.error("Failed to load word list", e);
            throw new RuntimeException("Could not load word list", e);
        }
        return words;
    }

    /**
     * For MVP, fetches a random 5-letter word by getting fetching a word at a random.nextInt() index of
     * the commonWords list instantiated when this service is initialized
     *
     * @return a String of length 5 from the word list.
     */
    public String getRandomWord() {
        if (commonWords.isEmpty()) {
            throw new IllegalStateException("Word list is empty");
        } else {
            try {
                int index = random.nextInt(commonWords.size());
                return commonWords.get(index);
            } catch (Exception e) {
                log.error("Failed to get random word from word list; using hardcoded fallback {}", e.getMessage());
                return FALLBACK_WORD;
            }
        }
    }

}
