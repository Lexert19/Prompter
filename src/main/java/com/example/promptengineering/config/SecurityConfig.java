package com.example.promptengineering.config;

import com.example.promptengineering.component.CustomAuthenticationEntryPoint;
import com.example.promptengineering.component.CustomAuthenticationSuccessHandler;
import com.example.promptengineering.filter.RateLimitingFilter;
import com.example.promptengineering.service.CustomOAuth2UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig implements WebMvcConfigurer {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RateLimitingFilter rateLimitingFilter;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
            RateLimitingFilter rateLimitingFilter,
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.rateLimitingFilter = rateLimitingFilter;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http,
                                                      AuthenticationManager authenticationManager)
            throws Exception {
        http.addFilterBefore(rateLimitingFilter,
                UsernamePasswordAuthenticationFilter.class);

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler));

        http.authorizeHttpRequests(exchanges -> exchanges
                .requestMatchers("/", "/{lang:(?:pl|en)}/**", "/public/**", "/login",
                        "/debug", "/error", "/terms", "/privacy", "/static/**",
                        "/auth/**", "/favicon.ico", "/favicon")
                .permitAll().requestMatchers("/admin/**", "/api/admin/**")
                .hasAuthority("ROLE_ADMIN").anyRequest().authenticated());

        http.oauth2Login(oauth2 -> oauth2.loginPage("/auth/login")
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService())
                        .userService(customOAuth2UserService))
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("OAuth2 login failed for request: {}",
                            request.getRequestURI(), exception);
                    response.sendRedirect("/auth/login?error=true");
                }));

        http.formLogin(customizer -> customizer.loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler()));

        http.logout(customizer -> customizer.logoutUrl("/auth/logout"));

        http.exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**"))
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/account/**"))
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/client/**"))
                .authenticationEntryPoint(authenticationEntryPoint()));

        return http.build();
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> response
                .sendRedirect("/auth/login?error=true");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint("/auth/login");
    }
}
