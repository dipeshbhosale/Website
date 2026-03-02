package com.skillnest.course.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads X-User-Id / X-User-Email / X-User-Role headers injected by the API Gateway
 * and makes them available as request attributes for controllers.
 */
@Component
@Order(1)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String uid   = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String role  = request.getHeader("X-User-Role");

        if (uid != null) {
            request.setAttribute("uid",   uid);
            request.setAttribute("email", email);
            request.setAttribute("role",  role);
        }

        chain.doFilter(request, response);
    }
}
