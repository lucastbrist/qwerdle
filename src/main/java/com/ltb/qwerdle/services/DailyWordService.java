package com.ltb.qwerdle.services;

import com.ltb.qwerdle.exceptions.DictionaryServiceException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyWordService {

    @Getter
    private volatile String dailyWord;

    private final DictionaryService dictionaryService;
    private final WordListService wordListService;

    /**
     * Initializes the daily word on application startup by delegating to {@link #populateDailyWord()}.
     * Ensures {@code dailyWord} is never null when the application begins serving requests.
     */
    @Async
    @PostConstruct
    public void initializeDailyWord() {
        populateDailyWord();
    }

    /**
     * Fetches a new random 5-letter word from the dictionary API and caches it as the daily word.
     * Runs automatically at midnight every day via Spring's task scheduler.
     * Also called on startup by {@link #initializeDailyWord()}.
     */
    @Async
    @Scheduled(cron = "@midnight")
    public void populateDailyWord() {

        try {
            this.dailyWord = dictionaryService.getRandomWord(5);
            log.info("Successfully populated daily word: {}", this.getDailyWord());
        } catch (DictionaryServiceException | IllegalArgumentException e) {
            log.error("Failed to populate daily: {}", e.getMessage());
            this.dailyWord = wordListService.getRandomWord();
            log.info("Populated daily word with fallback word: {}", this.getDailyWord());
        }
    }

}
