package com.example.promptengineering.controller;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ModelAndView listUsers() {
        List<User> users = userRepository.findAll();
        ModelAndView mav = new ModelAndView("admin/users");
        mav.addObject("users", users);
        mav.addObject("availableRoles", AppRole.values());
        return mav;
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam AppRole role) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRoles(List.of(role));
            userRepository.save(user);
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             AuthenticationPrincipal authenticationPrincipal,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) authenticationPrincipal;

        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
            return "redirect:/admin/users";
        }

        if (!userRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "User deleted successfully.");
        return "redirect:/admin/users";
    }
}
