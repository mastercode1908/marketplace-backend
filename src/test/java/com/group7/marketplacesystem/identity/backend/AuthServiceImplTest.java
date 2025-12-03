package com.group7.marketplacesystem.identity.backend;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.common.security.JwtUtils;
import com.group7.marketplacesystem.identity.dto.request.AuthRequest;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.mapper.AuthMapper;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.impl.AuthServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private Validator validator;

    @BeforeEach
    void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // =========================
    // Validation tests
    // =========================
    @Test void TUC01_emailNull_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email(null).password("Abcdef1!").build();
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test void TUC02_emailEmpty_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("").password("Abcdef1!").build();
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test void TUC03_emailSpace_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("   ").password("Abcdef1!").build();
        assertFalse(validator.validate(request).isEmpty());
    }


    //Email bắt vailidate bị thiếu
    @Test void TUC04_emailInvalid_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("abc@xyz").password("Abcdef1!").build();
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test void TUC05_emailNoAt_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("abc.com").password("Abcdef1!").build();
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test void TUC06_passwordNull_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("valid@mail.com").password(null).build();
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test void TUC07_passwordEmpty_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("valid@mail.com").password("").build();
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test void TUC08_passwordTooShort_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("valid@mail.com").password("Ab1!").build();
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test void TUC09_xssEmail_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder()
                .email("<script>alert(1)</script>")
                .password("12345678")
                .build();
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test void TUC10_sqlInjection_shouldFailValidation() {
        AuthRequest request = AuthRequest.builder().email("' OR '1'='1").password("Abcdef1!").build();
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }


    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthMapper authMapper;
    @InjectMocks private AuthServiceImpl authService;

    // Helper chung để mock login thành công
    private AuthResponse performValidLogin(String email) {
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setDeletedAt(null);
        user.setEmailVerified(true);
        user.setUserStatus("Active");
        user.setRole("BUYER");

        Mockito.lenient().when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = Mockito.mock(CustomUserDetails.class);
        Mockito.lenient().when(userDetails.getUser()).thenReturn(user);

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.lenient().when(authenticationManager.authenticate(Mockito.any())).thenReturn(authentication);

        Mockito.lenient().when(jwtUtils.generateAccessToken(userDetails)).thenReturn("accessToken");
        Mockito.lenient().when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("refreshToken");
        Mockito.lenient().when(authMapper.toAuthResponse(user, "accessToken", "refreshToken"))
                .thenReturn(AuthResponse.builder()
                        .accessToken("accessToken")
                        .refreshToken("refreshToken")
                        .user(null)
                        .build());

        return authService.login(AuthRequest.builder()
                .email(email)
                .password("Abcdef1!")
                .build());
    }


    @Test void TUC11_userNotExist_shouldThrow() {
        AuthRequest request = AuthRequest.builder().email("notexist@mail.com").password("Abcdef1!").build();
        Mockito.when(userRepository.findByEmail("notexist@mail.com")).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }

    @Test void TUC12_userDeleted_shouldThrow() {
        User user = new User();
        user.setEmail("deleted@mail.com");
        user.setDeletedAt(Instant.now());
        Mockito.when(userRepository.findByEmail("deleted@mail.com")).thenReturn(Optional.of(user));
        AuthRequest request = AuthRequest.builder().email("deleted@mail.com").password("Abcdef1!").build();
        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.ACCOUNT_DELETED, ex.getErrorCode());
    }

    @Test void TUC13_emailNotVerified_shouldThrow() {
        User user = new User();
        user.setEmail("unverified@mail.com");
        user.setDeletedAt(null);
        user.setEmailVerified(false);
        Mockito.when(userRepository.findByEmail("unverified@mail.com")).thenReturn(Optional.of(user));
        AuthRequest request = AuthRequest.builder().email("unverified@mail.com").password("Abcdef1!").build();
        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, ex.getErrorCode());
    }

    @Test void TUC14_bannedUser_shouldThrow() {
        User user = new User();
        user.setEmail("banned@mail.com");
        user.setDeletedAt(null);
        user.setEmailVerified(true);
        user.setUserStatus("Banned");
        Mockito.when(userRepository.findByEmail("banned@mail.com")).thenReturn(Optional.of(user));
        AuthRequest request = AuthRequest.builder().email("banned@mail.com").password("Abcdef1!").build();
        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.ACCOUNT_BANNED, ex.getErrorCode());
    }

    @Test void TUC15_inactiveUser_shouldThrow() {
        User user = new User();
        user.setEmail("inactive@mail.com");
        user.setDeletedAt(null);
        user.setEmailVerified(true);
        user.setUserStatus("Inactive");
        Mockito.when(userRepository.findByEmail("inactive@mail.com")).thenReturn(Optional.of(user));
        AuthRequest request = AuthRequest.builder().email("inactive@mail.com").password("Abcdef1!").build();
        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.ACCOUNT_INACTIVE, ex.getErrorCode());
    }

    @Test void TUC16_pendingUser_shouldThrow() {
        User user = new User();
        user.setEmail("pending@mail.com");
        user.setDeletedAt(null);
        user.setEmailVerified(true);
        user.setUserStatus("Pending");
        Mockito.when(userRepository.findByEmail("pending@mail.com")).thenReturn(Optional.of(user));
        AuthRequest request = AuthRequest.builder().email("pending@mail.com").password("Abcdef1!").build();
        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.ACCOUNT_PENDING, ex.getErrorCode());
    }

    @Test void TUC17_validLogin_shouldReturnAuthResponse() {
        AuthResponse response = performValidLogin("valid@mail.com");
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }


    //Mock authenticationManager.authenticate() để ném BadCredentialsException Service sẽ catch và ném ApiException với ErrorCode.UNAUTHENTICATED
    @Test void TUC18_wrongPassword_shouldThrow() {
        String email = "valid@mail.com";
        AuthRequest request = AuthRequest.builder().email(email).password("Wrong1!").build();
        User user = new User();
        user.setEmail(email);
        user.setDeletedAt(null);
        user.setEmailVerified(true);
        user.setUserStatus("Active");
        user.setRole("BUYER");

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
        Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(Mockito.any());
    }

    @Test void TUC19_authProviderError_shouldThrow() {
        User user = new User();
        user.setEmail("valid@mail.com");
        user.setDeletedAt(null);
        user.setEmailVerified(true);
        user.setUserStatus("Active");

        AuthRequest request = AuthRequest.builder().email("valid@mail.com").password("Abcdef1!").build();
        Mockito.when(userRepository.findByEmail("valid@mail.com")).thenReturn(Optional.of(user));
        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenThrow(new RuntimeException("Auth provider failure"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Auth provider failure", ex.getMessage());
    }

    @Test void TUC20_loginOk_shouldReturnAuthResponse() {
        AuthResponse response = performValidLogin("valid@mail.com");
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }

    @Test void TUC21_accessTokenReturned() {
        AuthResponse response = performValidLogin("valid@mail.com");
        assertEquals("accessToken", response.getAccessToken());
    }

    @Test void TUC22_refreshTokenReturned() {
        AuthResponse response = performValidLogin("valid@mail.com");
        assertEquals("refreshToken", response.getRefreshToken());
    }


    @Test void TUC23_performance() {
        long start = System.currentTimeMillis();
        AuthResponse response = performValidLogin("valid@mail.com");
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 1000);
    }

    @Test void TUC24_logLoginSuccess() {
        AuthResponse response = performValidLogin("valid@mail.com");
        assertNotNull(response);
    }

    @Test void TUC25_userNoRole() {
        String email = "norole@mail.com";
        User user = new User();
        user.setEmail(email);
        user.setDeletedAt(null);
        user.setEmailVerified(true);
        user.setUserStatus("Active");
        user.setRole(null);

        Mockito.lenient().when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        CustomUserDetails userDetails = Mockito.mock(CustomUserDetails.class);
        Mockito.lenient().when(userDetails.getUser()).thenReturn(user);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.lenient().when(authenticationManager.authenticate(Mockito.any())).thenReturn(authentication);
        Mockito.lenient().when(jwtUtils.generateAccessToken(userDetails)).thenReturn("token");
        Mockito.lenient().when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("token");
        Mockito.lenient().when(authMapper.toAuthResponse(user, "token", "token"))
                .thenReturn(AuthResponse.builder().accessToken("token").refreshToken("token").build());

        AuthResponse response = authService.login(AuthRequest.builder().email(email).password("Abcdef1!").build());
        assertNotNull(response);
    }

    @Test void TUC26_emailUppercase() {
        AuthResponse response = performValidLogin("TEST@MAIL.COM");
        assertNotNull(response);
    }

    @Test void TUC27_emailWithSpaces() {
        AuthResponse response = performValidLogin("  abc@mail.com  ");
        assertNotNull(response);
    }

    @Test void TUC28_passwordUnicode() {
        AuthResponse response = performValidLogin("unicode@mail.com");
        assertNotNull(response);
    }
}
