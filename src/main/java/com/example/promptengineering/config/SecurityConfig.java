package com.example.promptengineering.config;

import com.example.promptengineering.component.CustomAuthenticationEntryPoint;
import com.example.promptengineering.component.CustomAuthenticationSuccessHandler;
import com.example.promptengineering.component.OAuth2JwtSuccessHandler;
import com.example.promptengineering.filter.JwtAuthenticationFilter;
import com.example.promptengineering.filter.RateLimitingFilter;
import com.example.promptengineering.service.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig implements WebMvcConfigurer {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RateLimitingFilter rateLimitingFilter;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final OAuth2JwtSuccessHandler oAuth2JwtSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
            RateLimitingFilter rateLimitingFilter,
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
            OAuth2JwtSuccessHandler oAuth2JwtSuccessHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.rateLimitingFilter = rateLimitingFilter;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.oAuth2JwtSuccessHandler = oAuth2JwtSuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http,
                                                      AuthenticationManager authenticationManager)
            throws Exception {
        http.addFilterBefore(rateLimitingFilter,
                UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(
                        sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable);

        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(exchanges -> exchanges
            .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                .requestMatchers("/", "/{lang:(?:pl|en)}/**", "/public/**", "/login",
                        "/debug", "/error", "/terms", "/privacy", "/static/**",
                        "/auth/**", "/favicon.ico", "/favicon")
                .permitAll().requestMatchers("/admin/**", "/api/admin/**")
                .hasAuthority("ROLE_ADMIN").anyRequest().authenticated());

        http.oauth2Login(oauth2 -> oauth2.loginPage("/auth/login")
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService())
                        .userService(customOAuth2UserService))
                .successHandler(oAuth2JwtSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("OAuth2 login failed for request: {}",
                            request.getRequestURI(), exception);
                    response.sendRedirect("/auth/login?error=true");
                }));

        http.exceptionHandling(
                exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher("/account/**"))
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher("/client/**"))
                        .authenticationEntryPoint(authenticationEntryPoint()));

        return http.build();
    }

    @Profile("loadtest")
    @Bean
    SecurityFilterChain loadtestSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/mock-ai/**").permitAll()
                .anyRequest().authenticated());
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

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("mvc-async-");
        executor.setVirtualThreads(true);

        configurer.setTaskExecutor(executor);
        configurer.setDefaultTimeout(300_000);
    }
}
