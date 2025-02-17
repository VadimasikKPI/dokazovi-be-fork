package com.softserveinc.dokazovi.service.impl;

import com.softserveinc.dokazovi.entity.PasswordResetTokenEntity;
import com.softserveinc.dokazovi.entity.UserEntity;
import com.softserveinc.dokazovi.entity.VerificationToken;
import com.softserveinc.dokazovi.exception.BadRequestException;
import com.softserveinc.dokazovi.exception.EntityNotFoundException;
import com.softserveinc.dokazovi.mapper.UserMapper;
import com.softserveinc.dokazovi.pojo.UserSearchCriteria;
import com.softserveinc.dokazovi.repositories.UserRepository;
import com.softserveinc.dokazovi.repositories.VerificationTokenRepository;
import com.softserveinc.dokazovi.service.MailSenderService;
import com.softserveinc.dokazovi.service.PasswordResetTokenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private Pageable pageable;
    @Mock
    PasswordResetTokenService passwordResetTokenService;
    @Mock
    MailSenderService mailSenderService;
    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void findExpertById() {
        Integer id = 1;
        UserEntity userEntity = UserEntity.builder()
                .id(id)
                .build();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(userEntity));
        userService.findExpertById(id);

        verify(userMapper).toUserDTO(userEntity);
    }

    @Test
    void getRandomExpertPreview() {
        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));

        when(userRepository.findRandomExperts(any(Pageable.class)))
                .thenReturn(userEntityPage);
        userService.findRandomExpertPreview(null, pageable);

        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void getRandomExpertPreviewByDirections() {
        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));
        Set<Integer> directionIds = Set.of(1, 2);

        when(userRepository.findRandomExpertsByDirectionsIdIn(anySet(), any(Pageable.class)))
                .thenReturn(userEntityPage);
        userService.findRandomExpertPreview(directionIds, pageable);

        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void findAllExperts_NotFiltered() {

        Set<Integer> set = new HashSet<>();
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();

        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(set);

        Page<UserEntity> userEntityPage = Page.empty();

        when(userRepository.findAll(pageable)).thenReturn(userEntityPage);

        assertEquals(userEntityPage, userService.findAllExperts(userSearchCriteria, pageable));

    }

    @Test
    void findAllExperts_ByName() {

        Set<Integer> set = new HashSet<>();
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setUserName("Ни");
        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(set);

        Page<UserEntity> userEntityPage = Page.empty();

        when(userRepository.findDoctorsByName(userSearchCriteria.getUserName(), pageable)).thenReturn(userEntityPage);

        assertEquals(userEntityPage, userService.findAllExperts(userSearchCriteria, pageable));

    }

    @Test
    void findAllExperts_ByRegions() {

        Set<Integer> setDir = new HashSet<>();
        Set<Integer> setReg = new HashSet<>();
        setReg.add(1);
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setDirections(setDir);
        userSearchCriteria.setRegions(setReg);

        Page<UserEntity> userEntityPage = Page.empty();

        when(userRepository.findDoctorsProfilesByRegionsIds(userSearchCriteria.getRegions(), pageable))
                .thenReturn(userEntityPage);

        assertEquals(userEntityPage, userService.findAllExperts(userSearchCriteria, pageable));
    }

    @Test
    void findAllExperts_ByDirections() {

        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        Set<Integer> setDir = new HashSet<>();
        Set<Integer> setReg = new HashSet<>();
        setDir.add(1);
        userSearchCriteria.setDirections(setDir);
        userSearchCriteria.setRegions(setReg);

        Page<UserEntity> userEntityPage = Page.empty();

        when(userRepository.findDoctorsProfilesByDirectionsIds(userSearchCriteria.getDirections(), pageable))
                .thenReturn(userEntityPage);

        assertEquals(userEntityPage, userService.findAllExperts(userSearchCriteria, pageable));
    }

    @Test
    void findAllExperts_ByDirectionsAndRegions() {

        Set<Integer> setDir = new HashSet<>();
        Set<Integer> setReg = new HashSet<>();
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        setDir.add(1);
        setReg.add(1);
        userSearchCriteria.setDirections(setDir);
        userSearchCriteria.setRegions(setReg);

        Page<UserEntity> userEntityPage = Page.empty();

        when(userRepository.findDoctorsProfiles(userSearchCriteria.getDirections(),
                userSearchCriteria.getRegions(), pageable)).thenReturn(userEntityPage);

        assertEquals(userEntityPage, userService.findAllExperts(userSearchCriteria, pageable));
    }

    @Test
    void findAllExperts_ByOtherConditions() {

        Set<Integer> setDir = new HashSet<>();
        Set<Integer> setReg = new HashSet<>();
        setReg.add(1);
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setUserName("Ни");
        userSearchCriteria.setDirections(setDir);
        userSearchCriteria.setRegions(setReg);

        assertThrows(EntityNotFoundException.class, () -> userService.findAllExperts(userSearchCriteria, pageable));
    }

    @Test
    void findAllExpertsByDirectionsAndRegions_NotFiltered() {
        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));

        Set<Integer> set = new HashSet<>();
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(set);

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(userEntityPage);

        userService.findAllExperts(userSearchCriteria, pageable);
        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void findAllExpertsByDirectionsAndRegions_FilteredByRegionsOnly() {
        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));
        Set<Integer> regionsIds = Set.of(1, 4, 6);
        Set<Integer> set = new HashSet<>();
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(regionsIds);

        when(userRepository.findDoctorsProfilesByRegionsIds(anySet(), any(Pageable.class)))
                .thenReturn(userEntityPage);
        userService.findAllExperts(userSearchCriteria, pageable);

        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void findAllExpertsByDirectionsAndRegions_FilteredByDirectionsOnly() {
        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));
        Set<Integer> directionsIds = Set.of(1, 4, 6);
        Set<Integer> set = new HashSet<>();
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setDirections(directionsIds);
        userSearchCriteria.setRegions(set);

        when(userRepository.findDoctorsProfilesByDirectionsIds(
                anySet(), any(Pageable.class)
        )).thenReturn(userEntityPage);

        userService.findAllExperts(userSearchCriteria, pageable);

        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void findAllExpertsByDirectionsAndRegions_FilteredByDirectionsAndByRegions() {
        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));
        Set<Integer> directionsIds = Set.of(1, 4, 6);
        Set<Integer> regionsIds = Set.of(1, 4, 6);
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();

        userSearchCriteria.setDirections(directionsIds);
        userSearchCriteria.setRegions(regionsIds);

        when(userRepository
                .findDoctorsProfiles(
                        anySet(), anySet(), any(Pageable.class))
        ).thenReturn(userEntityPage);
        userService.findAllExperts(userSearchCriteria, pageable);

        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void findAllExpertsByName() {

        Set<Integer> set = new HashSet<>();
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setUserName("B");
        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(set);

        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));

        when(userRepository
                .findDoctorsByName("B", pageable))
                .thenReturn(userEntityPage);

        userService.findAllExperts(userSearchCriteria, pageable);
        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void findAllExpertsByName_WhenNotFound_ThrowException() {

        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        Set<Integer> set = new HashSet<>();
        userSearchCriteria.setUserName("Иван");
        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(set);

        when(userRepository
                .findDoctorsByName("Иван", pageable))
                .thenThrow(new EntityNotFoundException("User does not exist"));

        assertThrows(EntityNotFoundException.class, () -> userService
                .findAllExperts(userSearchCriteria, pageable));
    }

    @Test
    void findAllExpertsByFirstNameAndLastName() {

        Set<Integer> set = new HashSet<>();

        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setUserName("И И");
        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(set);

        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));

        when(userRepository
                .findDoctorsByName(anyString(), any(Pageable.class)))
                .thenReturn(userEntityPage);
        userService.findAllExperts(userSearchCriteria, pageable);
        verify(userMapper, times(userEntityPage.getNumberOfElements())).toUserDTO(any(UserEntity.class));
    }

    @Test
    void findAllExpertsByFirstNameAndLastName_WhenNotFound_ThrowException() {

        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        Set<Integer> set = new HashSet<>();
        userSearchCriteria.setUserName("И И");
        userSearchCriteria.setDirections(set);
        userSearchCriteria.setRegions(set);

        when(userRepository
                .findDoctorsByName("И И", pageable))
                .thenThrow(new EntityNotFoundException("User does not exist"));

        assertThrows(EntityNotFoundException.class, () -> userService
                .findAllExperts(userSearchCriteria, pageable));
    }

    @Test
    void findAll() {
        userService.findAll(pageable);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void setEnableTrue() {
        UserEntity userEntity = UserEntity.builder()
                .id(1)
                .build();
        when(userRepository.findById(any(Integer.class))).thenReturn(Optional.of(userEntity));
        userService.setEnableTrue(userEntity);
        assertTrue(userEntity.getEnabled());
        verify(userRepository, times(1))
                .findById(any(Integer.class));
    }

    @Test
    void getVerificationToken() {
        String token = "950c9760-805e-449c-a966-2d0d5ebd86f4";
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .build();
        when(tokenRepository.findByToken(any(String.class))).thenReturn(verificationToken);
        verificationToken = userService.getVerificationToken(token);
        assertEquals(token, verificationToken.getToken());
        verify(tokenRepository, times(1))
                .findByToken(any(String.class));
    }

    @Test
    void createVerificationToken() {
        String token = "950c9760-805e-449c-a966-2d0d5ebd86f4";
        UserEntity userEntity = UserEntity.builder().build();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(userEntity)
                .build();
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);
        userService.createVerificationToken(userEntity, token);
        verify(tokenRepository, times(1))
                .save(any(VerificationToken.class));
        assertEquals(token, verificationToken.getToken());
        assertEquals(userEntity, verificationToken.getUser());
    }

    @Test
    void findUserByEmail() {
        String email = "some@some.com";
        UserEntity user = UserEntity.builder()
                .email(email)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
        UserEntity resultUser = userService.findByEmail(email);
        verify(userRepository, times(1)).findByEmail(email);
        assertEquals(email, resultUser.getEmail());
    }

    @Test
    void findAllUser() {
        Page<UserEntity> users = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);
        userService.findAll(pageable);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void getUserByIdTest() {
        Integer id = 1;
        UserEntity expected = UserEntity.builder().id(id).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(expected));
        UserEntity actual = userService.getById(id);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void findUserEntityByEmailTest() {
        UserEntity expected = UserEntity.builder()
                .id(1)
                .email("admin@mail.com")
                .build();
        when(userRepository.findUserEntityByEmail(any(String.class))).thenReturn(Optional.ofNullable(expected));
        UserEntity actual = userService.findUserEntityByEmail("admin@mail.com");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void updateUserEntityTest() {
        UserEntity expected = UserEntity.builder()
                .id(1)
                .email("admin@mail.com")
                .password("$2y$10$GtQSp.P.EyAtCgUD2zWLW.01OBz409TGPl/Jo3U30Tig3YbbpIFv2")
                .build();
        when(userRepository.findById(anyInt())).thenReturn(Optional.ofNullable(expected));
        String expectedEmail = "test@mail.com";
        UserEntity userEntity = userService.getById(1);
        userEntity.setEmail(expectedEmail);
        when(userRepository.save(any(UserEntity.class))).thenReturn(expected);
        Assertions.assertNull(expected.getEditedAt());
        UserEntity actual = userService.update(userEntity);
        Assertions.assertEquals(expectedEmail, actual.getEmail());
    }

    @Test
    void updateUserPasswordTest() {
        UserEntity expected = UserEntity.builder()
                .id(1)
                .email("admin@mail.com")
                .password("$2y$10$GtQSp.P.EyAtCgUD2zWLW.01OBz409TGPl/Jo3U30Tig3YbbpIFv2")
                .build();
        when(userRepository.findById(anyInt())).thenReturn(Optional.ofNullable(expected));
        String expectedPassword = "qwerty12345";
        when(passwordEncoder.encode(any(String.class))).thenReturn(expectedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(expected);
        UserEntity userEntity = userService.getById(1);
        PasswordResetTokenEntity tokenEntity = PasswordResetTokenEntity.builder()
                .id(1L)
                .userEntity(expected)
                .token("ef590bd8-e993-4153-8206-b963732bfeb9")
                .dateExpiration(LocalDateTime.now().plusMinutes(60))
                .build();
        userService.updatePassword(userEntity, expectedPassword, tokenEntity);
        Assertions.assertEquals(expectedPassword, expected.getPassword());
        verify(passwordResetTokenService, times(1)).delete(tokenEntity);
    }

    @Test
    void sendPasswordResetTokenTest() {
        UserEntity user = UserEntity.builder()
                .id(1)
                .email("admin@mail.com")
                .password("$2y$10$GtQSp.P.EyAtCgUD2zWLW.01OBz409TGPl/Jo3U30Tig3YbbpIFv2")
                .build();
        String contextPath = "http://localhost:3000";
        userService.sendPasswordResetToken(user, contextPath);
        verify(passwordResetTokenService, times(1))
                .createPasswordResetTokenForUser(any(UserEntity.class), anyString());
        verify(mailSenderService, times(1))
                .sendEmailWithToken(anyString(), anyString(), any(UserEntity.class));
    }

    @Test
    void updateUserEntityIsNull() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            userService.update(null);
        });

        assertEquals("Something went wrong!!!", exception.getMessage());
    }
}
