package com.example.promptengineering.controller;

import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.model.ResultType;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.AuditLogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminController(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
      this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    public ModelAndView listUsers() {
        List<User> users = userRepository.findAll();
        ModelAndView mav = new ModelAndView("admin/users");
        mav.addObject("users", users);
        mav.addObject("availableRoles", AppRole.values());
        return mav;
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam AppRole role) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<AppRole> roles = new ArrayList<>();
            roles.add(role);
            user.setRoles(roles);
            userRepository.save(user);

            AuditLog log = auditLogService.createAuditLog(
                ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId(),
                SecurityContextHolder.getContext().getAuthentication().getName(),
                ActionType.ROLE_UPDATE,
                ResultType.SUCCESS,
                id.toString(),
                "Role changed to " + role,
                null
            );

            auditLogService.logAsync(log);
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal User currentUser,
                             RedirectAttributes redirectAttributes) {

        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error",
                    "You cannot delete your own account!");

            AuditLog log = auditLogService.createAuditLog(
                currentUser.getId(),
                currentUser.getEmail(),
                ActionType.USER_DELETE,
                ResultType.FAILURE,
                id.toString(),
                "cannot delete your own account",
                null
            );

            auditLogService.logAsync(log);
            return "redirect:/admin/users";
        }

        if (!userRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "User deleted successfully.");

        AuditLog log = auditLogService.createAuditLog(
            currentUser.getId(),
            currentUser.getEmail(),
            ActionType.USER_DELETE,
            ResultType.SUCCESS,
            id.toString(),
            "User deleted",
            null
        );
        auditLogService.logAsync(log);

        return "redirect:/admin/users";
    }
}
