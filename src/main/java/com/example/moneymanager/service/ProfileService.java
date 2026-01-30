package com.example.moneymanager.service;

import com.example.moneymanager.dto.AuthDTO;
import com.example.moneymanager.dto.ProfileDTO;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ProfileRepository;
import com.example.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {



    private final JwtUtil jwtUtil;
    private final AppUserDetailsService userDetailsService;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO){


        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile.setPassword(passwordEncoder.encode(newProfile.getPassword()));
        newProfile = profileRepository.save(newProfile);
        //send activation email
        String activationLink = activationURL+"/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your Money Manager account";
        String emailBody = "Dear " + newProfile.getFullName() + ",\n\n" +
                "Please click the following link to activate your account:\n" +
                activationLink + "\n\n" +
                "Best regards,\n" +
                "Money Manager Team";

        emailService.sendEmail(newProfile.getEmail(), subject, emailBody);
        return toDTO(newProfile);


    }

    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(profileDTO.getPassword())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdDate(profileDTO.getCreatedDate())
                .updatedDate(profileDTO.getUpdatedDate())
                .build();
    }


    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdDate(profileEntity.getCreatedDate())
                .updatedDate(profileEntity.getUpdatedDate())
                .build();
    }

    public boolean activateProfile(String activationToken){
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                }).orElse(false);
    }


    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }


    public ProfileEntity getCurrentProfile(){
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         return profileRepository.findByEmail(authentication.getName())
                 .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }


    public ProfileDTO getPublicProfile(String email){
        ProfileEntity currentUser;
        if(email == null){
            currentUser = getCurrentProfile();
        }
        else{
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdDate(currentUser.getCreatedDate())
                .updatedDate(currentUser.getUpdatedDate())
                .build();

    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {

        try {
            // Authenticate using EMAIL + PASSWORD
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDTO.getEmail(),
                            authDTO.getPassword()
                    )
            );

            // Load UserDetails
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(authDTO.getEmail());

            // Generate JWT
            String token = jwtUtil.generateToken(userDetails);

            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

}
