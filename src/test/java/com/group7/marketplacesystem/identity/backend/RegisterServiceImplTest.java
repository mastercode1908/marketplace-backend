package com.group7.marketplacesystem.identity.backend;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.service.EmailTokenService;
import com.group7.marketplacesystem.identity.dto.request.UserRegisterRequest;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.entity.UserProvider;
import com.group7.marketplacesystem.identity.mapper.BuyerMapper;
import com.group7.marketplacesystem.identity.mapper.LocalUserMapper;
import com.group7.marketplacesystem.identity.mapper.SellerMapper;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.repository.UserProviderRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.impl.RegisterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterService Test Cases")
class RegisterServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BuyerMapper buyerMapper;

    @Mock
    private SellerMapper sellerMapper;

    @Mock
    private LocalUserMapper localUserMapper;

    @Mock
    private EmailTokenService emailTokenService;

    @Mock
    private UserProviderRepository userProviderRepository;

    @InjectMocks
    private RegisterServiceImpl registerService;

    private UserRegisterRequest validRequest;
    private User mockUser;
    private Buyer mockBuyer;
    private Seller mockSeller;
    private UserProvider mockUserProvider;

    @BeforeEach
    void setUp() {
        validRequest = UserRegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123@")
                .fullName("Test User")
                .role("BUYER")
                .build();

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");

        mockBuyer = new Buyer();
        mockBuyer.setUsers(mockUser);

        mockSeller = new Seller();
        mockSeller.setUsers(mockUser);

        mockUserProvider = new UserProvider();
        mockUserProvider.setUser(mockUser);
    }

    // ==================== SUCCESS CASES ====================

    @Test
    @DisplayName("Should register buyer successfully")
    void tc01_testRegisterBuyer_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(buyerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(buyerMapper.toBuyerEntity(any(User.class))).thenReturn(mockBuyer);
        when(buyerRepository.save(any(Buyer.class))).thenReturn(mockBuyer);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        AuthResponse response = registerService.registerUser(validRequest);

        // Assert
        assertThat(response).isNull(); // Service returns null as per implementation
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("encodedPassword");

        verify(userRepository).save(any(User.class));
        verify(buyerRepository).save(any(Buyer.class));
        verify(userProviderRepository).save(any(UserProvider.class));
        verify(emailTokenService).createAndSendVerificationToken(1, "test@example.com");
    }

    @Test
    @DisplayName("Should register seller successfully")
    void tc02_testRegisterSeller_Success() {
        // Arrange
        validRequest.setRole("SELLER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(sellerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(sellerMapper.toSellerEntity(any(User.class))).thenReturn(mockSeller);
        when(sellerRepository.save(any(Seller.class))).thenReturn(mockSeller);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        AuthResponse response = registerService.registerUser(validRequest);

        // Assert
        assertThat(response).isNull();
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("encodedPassword");
        verify(userRepository).save(any(User.class));
        verify(sellerRepository).save(any(Seller.class));
        verify(userProviderRepository).save(any(UserProvider.class));
        verify(emailTokenService).createAndSendVerificationToken(1, "test@example.com");
    }

    // ==================== VALIDATION ERROR CASES ====================

    @Test
    @DisplayName("Should throw exception when email already exists")
    void tc03_testRegisterUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registerService.registerUser(validRequest))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAIL_EXISTED);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void tc03_testRegisterUser_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registerService.registerUser(validRequest))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_EXISTED);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when role is invalid")
    void tc04_testRegisterUser_InvalidRole() {
        // Arrange
        validRequest.setRole("INVALID_ROLE");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> registerService.registerUser(validRequest))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ROLE);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(buyerRepository, never()).save(any(Buyer.class));
        verify(sellerRepository, never()).save(any(Seller.class));
    }


    @Test
    @DisplayName("Should throw exception when password has no uppercase letter")
    void tc05_testRegisterUser_PasswordNoUppercase() {
        // Arrange
        validRequest.setPassword("password123@");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> registerService.registerUser(validRequest))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NO_UPPERCASE);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw exception when password has no lowercase letter")
    void tc06_testRegisterUser_PasswordNoLowercase() {
        // Arrange
        validRequest.setPassword("PASSWORD123@");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> registerService.registerUser(validRequest))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NO_LOWERCASE);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw exception when password has no number")
    void tc07_testRegisterUser_PasswordNoNumber() {
        // Arrange
        validRequest.setPassword("Password@");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> registerService.registerUser(validRequest))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NO_NUMBER);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw exception when password has no special character")
    void tc08_testRegisterUser_PasswordNoSpecialChar() {
        // Arrange
        validRequest.setPassword("Password123");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> registerService.registerUser(validRequest))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NO_SPECIAL);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ==================== ROLE CASE-INSENSITIVE TESTS ====================

    @Test
    @DisplayName("Should register buyer with lowercase role")
    void tc09_testRegisterUser_RoleCaseInsensitive_Buyer() {
        // Arrange
        validRequest.setRole("buyer");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(buyerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(buyerMapper.toBuyerEntity(any(User.class))).thenReturn(mockBuyer);
        when(buyerRepository.save(any(Buyer.class))).thenReturn(mockBuyer);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        AuthResponse response = registerService.registerUser(validRequest);

        // Assert
        assertThat(response).isNull();
        verify(buyerRepository).save(any(Buyer.class));
        verify(sellerRepository, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("Should register seller with lowercase role")
    void tc10_testRegisterUser_RoleCaseInsensitive_Seller() {
        // Arrange
        validRequest.setRole("seller");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(sellerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(sellerMapper.toSellerEntity(any(User.class))).thenReturn(mockSeller);
        when(sellerRepository.save(any(Seller.class))).thenReturn(mockSeller);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        AuthResponse response = registerService.registerUser(validRequest);

        // Assert
        assertThat(response).isNull();
        verify(sellerRepository).save(any(Seller.class));
        verify(buyerRepository, never()).save(any(Buyer.class));
    }

    // ==================== REPOSITORY INTERACTION TESTS ====================

    @Test
    @DisplayName("Should verify password is encoded before saving")
    void tc11_testRegisterUser_PasswordEncoded() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(buyerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(buyerMapper.toBuyerEntity(any(User.class))).thenReturn(mockBuyer);
        when(buyerRepository.save(any(Buyer.class))).thenReturn(mockBuyer);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        registerService.registerUser(validRequest);

        // Assert
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("Should save user, buyer, and user provider for buyer registration")
    void tc12_testRegisterBuyer_AllRepositoriesCalled() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(buyerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(buyerMapper.toBuyerEntity(any(User.class))).thenReturn(mockBuyer);
        when(buyerRepository.save(any(Buyer.class))).thenReturn(mockBuyer);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        registerService.registerUser(validRequest);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(buyerRepository, times(1)).save(any(Buyer.class));
        verify(userProviderRepository, times(1)).save(any(UserProvider.class));
        verify(sellerRepository, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("Should save user, seller, and user provider for seller registration")
    void tc13_testRegisterSeller_AllRepositoriesCalled() {
        // Arrange
        validRequest.setRole("SELLER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(sellerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(sellerMapper.toSellerEntity(any(User.class))).thenReturn(mockSeller);
        when(sellerRepository.save(any(Seller.class))).thenReturn(mockSeller);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        registerService.registerUser(validRequest);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(sellerRepository, times(1)).save(any(Seller.class));
        verify(userProviderRepository, times(1)).save(any(UserProvider.class));
        verify(buyerRepository, never()).save(any(Buyer.class));
    }

    @Test
    @DisplayName("Should send email verification token after buyer registration")
    void tc14_testRegisterBuyer_EmailVerificationSent() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(buyerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(buyerMapper.toBuyerEntity(any(User.class))).thenReturn(mockBuyer);
        when(buyerRepository.save(any(Buyer.class))).thenReturn(mockBuyer);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        registerService.registerUser(validRequest);

        // Assert
        verify(emailTokenService, times(1)).createAndSendVerificationToken(
                eq(1),
                eq("test@example.com")
        );
    }

    @Test
    @DisplayName("Should send email verification token after seller registration")
    void tc15_testRegisterSeller_EmailVerificationSent() {
        // Arrange
        validRequest.setRole("SELLER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(sellerMapper.toUserEntity(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(sellerMapper.toSellerEntity(any(User.class))).thenReturn(mockSeller);
        when(sellerRepository.save(any(Seller.class))).thenReturn(mockSeller);
        when(localUserMapper.toUserProvider(any(User.class))).thenReturn(mockUserProvider);
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(mockUserProvider);

        // Act
        registerService.registerUser(validRequest);

        // Assert
        verify(emailTokenService, times(1)).createAndSendVerificationToken(
                eq(1),
                eq("test@example.com")
        );
    }
}
