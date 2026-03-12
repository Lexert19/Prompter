package com.example.promptengineering.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Objects;

@Controller
public class HomeController {
    private final LocaleResolver localeResolver;

    public HomeController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @GetMapping({"/{lang:(?:pl|en)}/", "/"})
    public String getHomePage(
            @PathVariable(required = false) String lang,
            HttpServletRequest request,
            HttpServletResponse response) {

        Locale newLocale;
        newLocale = Locale.forLanguageTag(Objects.requireNonNullElse(lang, "pl"));
        localeResolver.setLocale(request, response, newLocale);

        return "home";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
}
