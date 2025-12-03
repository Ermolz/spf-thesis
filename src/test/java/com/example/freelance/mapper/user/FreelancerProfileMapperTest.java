package com.example.freelance.mapper.user;

import com.example.freelance.domain.skill.Skill;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {FreelancerProfileMapperImpl.class})
class FreelancerProfileMapperTest {

    @Autowired
    private FreelancerProfileMapper freelancerProfileMapper;

    @Test
    void toResponse_WhenProfileIsValid_ShouldReturnResponse() {
        User user = new User();
        user.setId(77L);
        user.setEmail("freelancer@test.com");

        FreelancerProfile profile = getFreelancerProfile(user);

        FreelancerProfileResponse response = freelancerProfileMapper.toResponse(profile);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(77L);
        assertThat(response.getEmail()).isEqualTo("freelancer@test.com");

        assertThat(response.getSkills()).containsExactly("Java", "Spring");
    }

    private static FreelancerProfile getFreelancerProfile(User user) {
        Skill javaSkill = new Skill();
        javaSkill.setId(1L);
        javaSkill.setName("Java");

        Skill springSkill = new Skill();
        springSkill.setId(2L);
        springSkill.setName("Spring");

        FreelancerProfile profile = new FreelancerProfile();
        profile.setId(1L);
        profile.setUser(user);
        profile.setDisplayName("Java Guru");
        profile.setBio("Senior Developer");
        profile.setHourlyRate(new BigDecimal("50.00"));
        profile.setCurrency("USD");
        profile.setRating(new BigDecimal("4.9"));
        profile.setCompletedProjectsCount(10);

        profile.setSkills(List.of(javaSkill, springSkill));
        return profile;
    }
}