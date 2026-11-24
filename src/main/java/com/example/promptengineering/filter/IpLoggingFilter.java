package com.example.promptengineering.filter;

import com.example.promptengineering.service.IpHitBatchWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IpLoggingFilter extends OncePerRequestFilter {
    private final IpHitBatchWriter writer;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        writer.offer(System.currentTimeMillis(), request.getRemoteAddr());
        chain.doFilter(request, response);
    }
}