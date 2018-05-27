package de.adorsys.springoauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyRestController {


    @GetMapping({ "/user", "/me" })
    public String user() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return String.format("Hello %s my friend.", (authentication!=null && authentication.getPrincipal()!=null?authentication.getPrincipal().toString():"Unknown"));
    }
}
