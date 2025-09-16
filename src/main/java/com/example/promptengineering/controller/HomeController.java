package com.example.promptengineering.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Optional;

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

        Locale newLocale = Optional.ofNullable(lang)
                .map(Locale::forLanguageTag)
                .orElse(Locale.ENGLISH);

        localeResolver.setLocale(request, response, newLocale);

        return "home";
    }
}
