package com.attendance.controller;

import com.attendance.entity.User;
import com.attendance.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            return user.getRole().equals("ADMIN") ?
                    "redirect:/admin/dashboard" :
                    "redirect:/employee/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            if (userService.authenticate(username, password)) {
                User user = userService.getUserByUsername(username).orElse(null);
                if (user == null) {
                    model.addAttribute("error", "User not found");
                    return "login";
                }

                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("userRole", user.getRole());

                redirectAttributes.addFlashAttribute("success",
                        "Welcome " + user.getFirstName() + "!");

                return user.getRole().equals("ADMIN") ?
                        "redirect:/admin/dashboard" :
                        "redirect:/employee/dashboard";
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam String confirmPassword,
                           Model model) {
        try {
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                return "register";
            }

            if (userService.getUserByUsername(user.getUsername()).isPresent()) {
                model.addAttribute("error", "Username already exists");
                return "register";
            }

            if (userService.getUserByEmail(user.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already registered");
                return "register";
            }

            user.setRole("EMPLOYEE");
            user.setDateOfJoining(LocalDate.now());
            userService.saveUser(user);

            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Logged out successfully");
        return "redirect:/login";
    }
}