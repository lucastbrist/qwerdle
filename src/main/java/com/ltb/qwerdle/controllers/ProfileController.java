package com.ltb.qwerdle.controllers;

import com.ltb.qwerdle.models.CustomUserDetails;
import com.ltb.qwerdle.models.User;
import com.ltb.qwerdle.services.CustomUserDetailsService;
import com.ltb.qwerdle.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirect) {

        if (!password.equals(confirmPassword)) {
            redirect.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/register";
        }

        try {
            userService.registerUser(username, email, password);
            redirect.addFlashAttribute("message", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}