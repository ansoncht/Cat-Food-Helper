package com.ansoncht.catfoodtracker.user;

import com.ansoncht.catfoodtracker.user.dto.UserDTO;
import com.ansoncht.catfoodtracker.user.dto.UserLoginDTO;
import com.ansoncht.catfoodtracker.user.dto.UserRegistrationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class UserServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    private UserService userService;
    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.userService = new UserService(mockUserRepository);

    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testRegisterUser_ValidRequest_ShouldSucceed() {
        UserRegistrationDTO req = new UserRegistrationDTO("test", "test@gmail.com", "test", "test", "test");
        String encodedPassword = new BCryptPasswordEncoder().encode(req.getPassword());
        User expected = new User("test", "test@gmail.com", "test", "test", encodedPassword);

        when(mockUserRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(mockUserRepository.existsByUsername(req.getUsername())).thenReturn(false);
        when(mockUserRepository.save(expected)).thenReturn(expected);

        UserDTO actual = this.userService.registerUser(req);

        assertNotNull(actual);
        assertEquals("Response ID: ", expected.getId(), actual.getId());
        assertEquals("Response Username: ", expected.getUsername(), actual.getUsername());
        assertEquals("Response Email: ", expected.getEmail(), actual.getEmail());
        assertEquals("Response First Name: ", expected.getFirstName(), actual.getFirstName());
        assertEquals("Response Last Name: ", expected.getLastName(), actual.getLastName());

        verify(mockUserRepository).save(Mockito.any(User.class));
        verify(mockUserRepository).existsByEmail(req.getEmail());
        verify(mockUserRepository).existsByUsername(req.getUsername());
    }

    @Test
    public void testRegisterUser_ExistingUsername_ShouldThrowException() {
        UserRegistrationDTO req = new UserRegistrationDTO("test", "test@gmail.com", "test", "test", "test");

        when(mockUserRepository.existsByUsername(req.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(req));
        assertNotNull(exception.getMessage());

        verify(mockUserRepository).existsByUsername(req.getUsername());
    }

    @Test
    public void testRegisterUser_ExistingEmail_ShoudThrowException() {
        UserRegistrationDTO req = new UserRegistrationDTO("test", "test@gmail.com", "test", "test", "test");

        when(mockUserRepository.existsByUsername(req.getUsername())).thenReturn(false);
        when(mockUserRepository.existsByEmail(req.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(req));
        assertNotNull(exception.getMessage());

        verify(mockUserRepository).existsByEmail(req.getEmail());
        verify(mockUserRepository).existsByUsername(req.getUsername());
    }

    @Test
    public void testAuthenticateUser_ValidRequest_ShouldSucceed() {
        UserLoginDTO req = new UserLoginDTO("test@gmail.com", "password");
        String encodedPassword = new BCryptPasswordEncoder().encode(req.getPassword());
        User expected = new User("test", "test@gmail.com", "test", "test", encodedPassword);

        when(mockUserRepository.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())).thenReturn(Optional.of(expected));

        UserDTO actual = this.userService.authenticateUser(req);

        assertNotNull(actual);
        assertEquals("Response ID: ", actual.getId(), expected.getId());
        assertEquals("Response Username: ", actual.getUsername(), expected.getUsername());
        assertEquals("Response Email: ", actual.getEmail(), expected.getEmail());
        assertEquals("Response First Name: ", actual.getFirstName(), expected.getFirstName());
        assertEquals("Response Last Name: ", actual.getLastName(), expected.getLastName());

        verify(mockUserRepository).findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail());
    }

    @Test
    public void testAuthenticateUser_NonExistentUser_ShouldThrowException() {
        UserLoginDTO req = new UserLoginDTO("test@gmail.com", "test");

        when(mockUserRepository.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> this.userService.authenticateUser(req));
        assertNotNull(exception.getMessage());

        verify(mockUserRepository).findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail());
    }

    @Test
    public void testAuthenticateUser_IncorrectPassword_ShouldThrowException() {
        UserLoginDTO req = new UserLoginDTO("test@gmail.com", "wrongpassword");
        User expected = new User("test", "test@gmail.com", "test", "test", "encodedCorrectPassword");

        when(mockUserRepository.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())).thenReturn(Optional.of(expected));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> this.userService.authenticateUser(req));
        assertNotNull(exception.getMessage());

        verify(mockUserRepository).findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail());
    }

    @Test
    public void testLoadUserByUsername_ValidUsername_ShouldSucceed() {
        User expected = new User("test", "test@gmail.com", "test", "test", "encodedCorrectPassword");

        when(mockUserRepository.findByUsernameOrEmail(expected.getUsername(), expected.getUsername())).thenReturn(Optional.of(expected));

        UserDetails actual = this.userService.loadUserByUsername(expected.getUsername());

        assertNotNull(actual);
        assertEquals("Response Username: ", actual.getUsername(), expected.getUsername());

        verify(mockUserRepository).findByUsernameOrEmail(expected.getUsername(), expected.getUsername());
    }

    @Test
    void loadUserByUsername_NonExistentUser_ShouldThrowsException() {
        when(mockUserRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(UsernameNotFoundException.class, () -> this.userService.loadUserByUsername("testuser"));
        assertNotNull(exception.getMessage());

        verify(mockUserRepository).findByUsernameOrEmail(anyString(), anyString());
    }
}
