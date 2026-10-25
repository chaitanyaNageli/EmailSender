
package com.chaitanya.email_sender.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/auth/me")
    public Map<String, Object> getCurrentUser(
            @AuthenticationPrincipal OAuth2User user) {

        Map<String, Object> details = new HashMap<>();

        details.put("name", user.getAttribute("name"));
        details.put("email", user.getAttribute("email"));
        details.put("picture", user.getAttribute("picture"));

        return details;
    }
}
