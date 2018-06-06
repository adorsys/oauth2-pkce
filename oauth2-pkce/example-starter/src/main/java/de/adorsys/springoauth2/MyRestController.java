package de.adorsys.springoauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class MyRestController {

    @GetMapping({ "/user", "/me" })
    public Authentication user() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
