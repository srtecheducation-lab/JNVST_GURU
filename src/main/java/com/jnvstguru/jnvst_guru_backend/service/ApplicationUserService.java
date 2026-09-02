package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentProfileRequest;
import com.jnvstguru.jnvst_guru_backend.domain.DistrictEntity;
import com.jnvstguru.jnvst_guru_backend.domain.ExamSessionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.RoleEntity;
import com.jnvstguru.jnvst_guru_backend.domain.StateEntity;
import com.jnvstguru.jnvst_guru_backend.domain.StudentProfileEntity;
import com.jnvstguru.jnvst_guru_backend.domain.SubscriptionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserRoleEntity;
import com.jnvstguru.jnvst_guru_backend.repository.DistrictRepository;
import com.jnvstguru.jnvst_guru_backend.repository.ExamSessionRepository;
import com.jnvstguru.jnvst_guru_backend.repository.RoleRepository;
import com.jnvstguru.jnvst_guru_backend.repository.StateRepository;
import com.jnvstguru.jnvst_guru_backend.repository.StudentProfileRepository;
import com.jnvstguru.jnvst_guru_backend.repository.SubscriptionRepository;
import com.jnvstguru.jnvst_guru_backend.repository.UserRepository;
import com.jnvstguru.jnvst_guru_backend.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApplicationUserService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationUserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ExamSessionRepository examSessionRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ApplicationUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            StudentProfileRepository studentProfileRepository,
            ExamSessionRepository examSessionRepository,
            StateRepository stateRepository,
            DistrictRepository districtRepository,
            SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.examSessionRepository = examSessionRepository;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public UserEntity findOrCreateUser(UUID authUserId) {
        log.info("[USER] Looking up application user for authUserId={}", authUserId);
        return userRepository.findByAuthUserId(authUserId)
                .map(user -> {
                    log.info("[USER] Application user found: userId={}", user.getId());
                    return user;
                })
                .orElseGet(() -> {
                    log.info("[USER] Creating application user for authUserId={}", authUserId);
                    UserEntity user = new UserEntity();
                    user.setAuthUserId(authUserId);
                    user.setStatus("ACTIVE");
                    UserEntity savedUser = userRepository.save(user);
                    log.info("[USER] Application user created: userId={}", savedUser.getId());

                    RoleEntity studentRole = roleRepository.findByCode("STUDENT")
                            .orElseThrow(() -> new IllegalStateException("Required STUDENT role was not found."));

                    UserRoleEntity userRole = new UserRoleEntity(savedUser, studentRole);
                    userRoleRepository.save(userRole);
                    log.debug("[USER] Added STUDENT role to application user userId={}", savedUser.getId());
                    return savedUser;
                });
    }

    @Transactional(readOnly = true)
    public UserEntity findByAuthUserId(UUID authUserId) {
        log.info("[USER] Looking up application user for authUserId={}", authUserId);
        return userRepository.findByAuthUserId(authUserId)
                .map(user -> {
                    log.info("[USER] Application user found: userId={}", user.getId());
                    return user;
                })
                .orElseThrow(() -> {
                    log.warn("[USER] Application user not found for authUserId={}", authUserId);
                    return new IllegalStateException("Application user not found for auth_user_id=" + authUserId);
                });
    }

    @Transactional(readOnly = true)
    public List<String> getRoleCodesForUser(UUID authUserId) {
        UserEntity user = userRepository.findByAuthUserId(authUserId).orElse(null);
        if (user == null) {
            log.warn("[USER] No roles available for authUserId={} because application user does not exist", authUserId);
            return new ArrayList<>();
        }

        List<String> roleCodes = userRoleRepository.findByUserOrderByRoleCode(user)
                .stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
        log.debug("[USER] Loaded roles for userId={}: {}", user.getId(), roleCodes);
        return roleCodes;
    }

    @Transactional(readOnly = true)
    public StudentProfileEntity getStudentProfile(UserEntity user) {
        log.info("[STUDENT_PROFILE] Fetching profile for userId={}", user.getId());
        StudentProfileEntity profile = studentProfileRepository.findByUser(user).orElse(null);
        if (profile == null) {
            log.info("[STUDENT_PROFILE] No profile found for userId={}", user.getId());
        } else {
            log.info("[STUDENT_PROFILE] Profile found for userId={}: profileId={}", user.getId(), profile.getId());
            log.debug("[STUDENT_PROFILE] Loaded related stateId={}, districtId={}, examSessionId={}",
                    profile.getState() != null ? profile.getState().getId() : null,
                    profile.getDistrict() != null ? profile.getDistrict().getId() : null,
                    profile.getExamSession() != null ? profile.getExamSession().getId() : null);
        }
        return profile;
    }

    @Transactional(readOnly = true)
    public List<StateEntity> getActiveStates() {
        return stateRepository.findByStatusOrderByNameAsc("ACTIVE");
    }

    @Transactional(readOnly = true)
    public Optional<StateEntity> getStateById(Long stateId) {
        return stateRepository.findByIdAndStatus(stateId, "ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<DistrictEntity> getActiveDistrictsByState(StateEntity state) {
        return districtRepository.findByStateAndStatusOrderByNameAsc(state, "ACTIVE");
    }

    @Transactional(readOnly = true)
    public Optional<DistrictEntity> getDistrictById(Long districtId) {
        return districtRepository.findByIdAndStatus(districtId, "ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<SubscriptionEntity> getSubscriptions(UserEntity user) {
        log.info("[SUBSCRIPTION] Fetching subscriptions for userId={}", user.getId());
        return subscriptionRepository.findByUserOrderByStartAtDesc(user);
    }

    @Transactional
    public StudentProfileEntity createStudentProfile(UserEntity user, StudentProfileRequest request) {
        log.info("[STUDENT_PROFILE] Creating profile for userId={}", user.getId());
        if (studentProfileRepository.findByUser(user).isPresent()) {
            log.warn("[STUDENT_PROFILE] Student profile already exists for userId={}", user.getId());
            throw new StudentProfileAlreadyExistsException();
        }

        StateEntity state = stateRepository.findByIdAndStatus(request.stateId(), "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Invalid state id=" + request.stateId()));

        DistrictEntity district = districtRepository.findByIdAndStatus(request.districtId(), "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Invalid district id=" + request.districtId()));

        if (!district.getState().getId().equals(state.getId())) {
            log.warn("[STUDENT_PROFILE] Invalid district/state combination: stateId={} districtId={} districtStateId={}",
                    state.getId(), district.getId(), district.getState().getId());
            throw new IllegalArgumentException("District does not belong to selected state");
        }

        ExamSessionEntity examSession = examSessionRepository.findById(request.examSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam session id=" + request.examSessionId()));

        StudentProfileEntity profile = new StudentProfileEntity();
        profile.setUser(user);
        profile.setName(Objects.requireNonNull(request.name(), "name must not be null").trim());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setGender(request.gender());
        profile.setCategory(request.category());
        profile.setResidentialArea(request.residentialArea());
        profile.setClassLevel(request.classLevel());
        profile.setState(state);
        profile.setDistrict(district);
        profile.setPreferredLanguage(request.preferredLanguage().trim());
        profile.setExamSession(examSession);

        try {
            StudentProfileEntity savedProfile = studentProfileRepository.save(profile);
            log.info("[STUDENT_PROFILE] Profile created for userId={}: profileId={}", user.getId(), savedProfile.getId());
            return savedProfile;
        } catch (DataIntegrityViolationException ex) {
            log.warn("[STUDENT_PROFILE] Duplicate profile detected for userId={} during create", user.getId());
            throw new StudentProfileAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public UserEntity findById(Long userId) {
        log.debug("[USER] Fetching user by id={}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id=" + userId));
    }
}
