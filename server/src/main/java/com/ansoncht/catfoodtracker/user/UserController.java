package com.ansoncht.catfoodtracker.user;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ansoncht.catfoodtracker.security.JwtService;
import com.ansoncht.catfoodtracker.user.dto.UserDTO;
import com.ansoncht.catfoodtracker.user.dto.UserLoginDTO;
import com.ansoncht.catfoodtracker.user.dto.UserRegistrationDTO;

import jakarta.validation.Valid;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/v1/user/signup")
    public ResponseEntity<HashMap<String, Object>> signUp(@Valid @RequestBody UserRegistrationDTO req) {
        logger.info("Signing up user: {}", req.getUsername());

        try {
            UserDTO res = this.userService.registerUser(req);
            String token = this.jwtService.generateToken(res.getUsername());

            logger.info("User creation succeeded for: {}", req.getUsername());

            HashMap<String, Object> response = new HashMap<>();
            response.put("user", res);
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("User creation failed for: {}", req.getUsername());

            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/v1/user/signin")
    public ResponseEntity<HashMap<String, Object>> signIn(@Valid @RequestBody UserLoginDTO req) {
        logger.info("Signing in user: {}", req.getUsernameOrEmail());

        try {
            UserDTO res = this.userService.authenticateUser(req);
            String token = this.jwtService.generateToken(res.getUsername());

            logger.info("User login succeeded for: {}", req.getUsernameOrEmail());

            HashMap<String, Object> response = new HashMap<>();
            response.put("user", res);
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("User login failed for: {}", req.getUsernameOrEmail());

            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/api/v1/user/protected")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("This is a protected endpoint. You are authenticated!");
    }

}
