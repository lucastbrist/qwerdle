package com.ltb.qwerdle.controllers;

import com.ltb.qwerdle.exceptions.DictionaryServiceException;
import com.ltb.qwerdle.models.CustomUserDetails;
import com.ltb.qwerdle.services.DictionaryService;
import com.ltb.qwerdle.services.UserService;
import com.ltb.qwerdle.services.WordListService;
import com.ltb.qwerdle.services.WordService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes("game")
public class QwerdleGameController {

    private final WordService wordService;
    private final WordListService wordListService;
    private final UserService userService;

    @GetMapping
    public String home() {
        return "redirect:/play";
    }

    @GetMapping("/play")
    public String play(@ModelAttribute("game") GameState game, Model model,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (game.answer == null || game.isComplete) {
            game.reset();
            game.answer = wordListService.getRandomWord();
            game.sessionId = UUID.randomUUID().toString();
            // log.info("New game started for {}: {}", userDetails.getUsername(), game.answer.toUpperCase());
        }

        model.addAttribute("currentRow", game.guesses.size());
        model.addAttribute("sessionId", game.sessionId);
        model.addAttribute("wordLength", 5);
        model.addAttribute("maxAttempts", 6);
        model.addAttribute("guesses", game.guesses);
        model.addAttribute("completed", game.isComplete);
        model.addAttribute("won", game.isWon);
        // model.addAttribute("username", userDetails.getUsername());

        return "play";
    }

    @PostMapping("/guess")
    public String guess(@RequestParam String guess,
                        @ModelAttribute("game") GameState game,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirect) {

        if (game.isComplete) {
            redirect.addFlashAttribute("error", "Game already finished");
            return "redirect:/play";
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
                userService.recordWin(userDetails.getUsername());
                redirect.addFlashAttribute("message", "You won! The word was: " + game.answer.toUpperCase());
            }
            // Check if lost
            else if (game.guesses.size() >= 6) {
                game.isComplete = true;
                userService.recordLoss(userDetails.getUsername());
                redirect.addFlashAttribute("error", "Game over! The word was: " + game.answer.toUpperCase());
            }

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (DictionaryServiceException e) {
            redirect.addFlashAttribute("error", "Could not validate word");
            log.error("Dictionary error: {}", e.getMessage());
        }

        return "redirect:/play";
    }

    @ModelAttribute("game")
    public GameState game() {
        return new GameState();
    }

    @Data
    public static class GameState {
        String sessionId;
        String answer;
        List<GuessResult> guesses = new ArrayList<>();
        boolean isComplete = false;
        boolean isWon = false;

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