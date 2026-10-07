package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.GameData.Service.AccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Signs a request in when it carries a valid "Authorization: Bearer ..." token.
 * The authenticated principal is the player's id. Requests without a token
 * simply stay anonymous (guests).
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AccountService accounts;

    public TokenAuthenticationFilter(AccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            accounts.authenticate(header.substring(7).trim()).ifPresent(playerId ->
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(playerId, null, List.of())));
        }
        chain.doFilter(request, response);
    }
}
