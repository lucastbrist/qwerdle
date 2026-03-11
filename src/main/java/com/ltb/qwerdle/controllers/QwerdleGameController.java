package com.ltb.qwerdle.controllers;

import com.ltb.qwerdle.exceptions.DictionaryServiceException;
import com.ltb.qwerdle.models.CustomUserDetails;
import com.ltb.qwerdle.services.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({"randomGame", "dailyGame"})
public class QwerdleGameController {

    private final WordService wordService;
    private final WordListService wordListService;
    private final UserService userService;
    private final DailyWordService dailyWordService;

    @GetMapping("/play")
    public String play(
                        @RequestParam(defaultValue = "false") boolean daily,
                        @RequestParam(defaultValue = "false") boolean newGame,
                        @ModelAttribute("randomGame") GameState randomGame,
                        @ModelAttribute("dailyGame") GameState dailyGame,
                        Model model,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {

        GameState game = daily ? dailyGame : randomGame;

        boolean shouldReset = (game.answer == null) || (game.isComplete && newGame);
        boolean dailyAlreadyStarted = (daily && LocalDate.now().equals(game.dailyLastPlayed));

        if (!dailyAlreadyStarted && shouldReset) {
            game.reset();
            game.answer = daily ? dailyWordService.getDailyWord() : wordListService.getRandomWord();
            if (daily) game.dailyLastPlayed = LocalDate.now();
            logNewGame(userDetails, game.answer);
        }

        model.addAttribute("currentRow", game.guesses.size());
        model.addAttribute("wordLength", 5);
        model.addAttribute("maxAttempts", 6);
        model.addAttribute("guesses", game.guesses);
        model.addAttribute("completed", game.isComplete);
        model.addAttribute("won", game.isWon);
        model.addAttribute("daily", daily);
        if (game.isComplete) {
            model.addAttribute("answer", game.answer.toUpperCase());
        }
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }

        return "play";
    }

    @PostMapping("/guess")
    public String guess(@RequestParam String guess,
                        @RequestParam(defaultValue = "false") boolean daily,
                        @ModelAttribute("randomGame") GameState randomGame,
                        @ModelAttribute("dailyGame") GameState dailyGame,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirect) {

        GameState game = daily ? dailyGame : randomGame;

        if (game.isComplete) {
            redirect.addFlashAttribute("error", "Game already finished");
            return "redirect:/play" + (daily ? "?daily=true" : "");
        }

        try {
            // Convert guess to character list
            List<Character> chars = guess.chars()
                    .mapToObj(c -> (char) c)
                    .collect(Collectors.toList());

            // Get feedback from word service
            List<Character> feedback = wordService.handleGuess(chars, game.answer);

            // Convert to string
            String feedbackString = feedback.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining());

            // Store this guess
            game.guesses.add(new GuessResult(guess.toUpperCase(), feedbackString));

            // Check if won
            if (feedback.stream().allMatch(c -> c == 'C')) {
                game.isWon = true;
                game.isComplete = true;
                if (userDetails != null) {
                    userService.recordWin(userDetails.getUsername());
                }

            }
            // Check if lost
            else if (game.guesses.size() >= 6) {
                game.isComplete = true;
                if (userDetails != null) {
                    userService.recordLoss(userDetails.getUsername());
                }
            }

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (DictionaryServiceException e) {
            redirect.addFlashAttribute("error", "Could not validate word");
            log.error("Dictionary error: {}", e.getMessage());
        }

        return "redirect:/play" + (daily ? "?daily=true" : "");
    }

    private void logNewGame(CustomUserDetails userDetails, String answer) {
        if (userDetails != null) {
            log.info("New game started for {}: {}", userDetails.getUsername(), answer.toUpperCase());
        } else {
            log.info(answer.toUpperCase());
        }
    }

    @ModelAttribute("randomGame")
    public GameState randomGame() {
        return new GameState();
    }

    @ModelAttribute("dailyGame")
    public GameState dailyGame() {
        GameState game = new GameState();
        game.isDaily = true;
        return game;
    }

    @Data
    public static class GameState {
        String answer;
        List<GuessResult> guesses = new ArrayList<>();
        boolean isComplete = false;
        boolean isWon = false;
        boolean isDaily;
        LocalDate dailyLastPlayed;

        void reset() {
            guesses.clear();
            isComplete = false;
            isWon = false;
        }
    }

    @Data
    public static class GuessResult {
        final String guess;
        final String feedback;

        // Thymeleaf needs this alias
        public String getWord() {
            return guess;
        }

    }
}