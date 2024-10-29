package com.ansoncht.catfoodtracker.user;

import com.ansoncht.catfoodtracker.config.TestSecurityConfig;
import com.ansoncht.catfoodtracker.security.JwtService;
import com.ansoncht.catfoodtracker.user.dto.UserDTO;
import com.ansoncht.catfoodtracker.user.dto.UserLoginDTO;
import com.ansoncht.catfoodtracker.user.dto.UserRegistrationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 59, 59);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSignUp_ValidRequest_ShouldSucceedWithOk() throws Exception {

        UserRegistrationDTO req = new UserRegistrationDTO("test", "test@gmail.com", "test", "test", "testPassword");
        UserDTO expected = new UserDTO("1L", "test", "test@gmail.com", "test", "test", FIXED_TIME, FIXED_TIME);

        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(expected);

        mockMvc.perform(post("/api/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(expected.getId()))
                .andExpect(jsonPath("$.user.username").value(expected.getUsername()))
                .andExpect(jsonPath("$.user.email").value(expected.getEmail()))
                .andExpect(jsonPath("$.user.firstName").value(expected.getFirstName()))
                .andExpect(jsonPath("$.user.lastName").value(expected.getLastName()))
                .andExpect(jsonPath("$.user.createdAt").value(expected.getCreatedAt().toString()))
                .andExpect(jsonPath("$.user.updatedAt").value(expected.getUpdatedAt().toString()));

        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void testSignUp_InvalidRequest_ShouldFailWithBadRequest() throws Exception {

        UserRegistrationDTO req = new UserRegistrationDTO("test", "test@gmail.com", "test", "test", "test");

        mockMvc.perform(post("/api/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignUp_ExistingUsername_ShouldFailWithBadRequest() throws Exception {
        UserRegistrationDTO req = new UserRegistrationDTO("test", "test@gmail.com", "test", "test", "testPassword");

        when(userService.registerUser(any(UserRegistrationDTO.class))).thenThrow(new RuntimeException("Test RuntimeException"));

        mockMvc.perform(post("/api/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void testSignIn_ValidRequest_ShouldSucceedWithOk() throws Exception {

        UserLoginDTO req = new UserLoginDTO("test", "testPassword");
        UserDTO expected = new UserDTO("1L", "test", "test@gmail.com", "test", "test", FIXED_TIME, FIXED_TIME);

        when(userService.authenticateUser(any(UserLoginDTO.class))).thenReturn(expected);

        mockMvc.perform(post("/api/v1/user/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(expected.getId()))
                .andExpect(jsonPath("$.user.username").value(expected.getUsername()))
                .andExpect(jsonPath("$.user.email").value(expected.getEmail()))
                .andExpect(jsonPath("$.user.firstName").value(expected.getFirstName()))
                .andExpect(jsonPath("$.user.lastName").value(expected.getLastName()))
                .andExpect(jsonPath("$.user.createdAt").value(expected.getCreatedAt().toString()))
                .andExpect(jsonPath("$.user.updatedAt").value(expected.getUpdatedAt().toString()));

        verify(userService).authenticateUser(any(UserLoginDTO.class));
    }

    @Test
    void testSignIn_InvalidRequest_ShouldFailWithBadRequest() throws Exception {
        UserLoginDTO req = new UserLoginDTO("test", "test");

        mockMvc.perform(post("/api/v1/user/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignUp_IncorrectPassword_ShouldFailWithBadRequest() throws Exception {
        UserLoginDTO req = new UserLoginDTO("test", "testPassword");

        when(userService.authenticateUser(any(UserLoginDTO.class)))
                .thenThrow(new RuntimeException("Test RuntimeException"));
        when(userService.authenticateUser(any(UserLoginDTO.class)))
                .thenThrow(new RuntimeException("Test RuntimeException"));

        mockMvc.perform(post("/api/v1/user/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(userService).authenticateUser(any(UserLoginDTO.class));
    }
}