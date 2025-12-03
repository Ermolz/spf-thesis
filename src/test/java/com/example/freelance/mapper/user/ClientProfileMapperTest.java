package com.example.freelance.mapper.user;

import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.user.ClientProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ClientProfileMapperImpl.class})
class ClientProfileMapperTest {

    @Autowired
    private ClientProfileMapper clientProfileMapper;

    @Test
    void toResponse_WhenProfileIsValid_ShouldReturnResponse() {
        User user = new User();
        user.setId(88L);
        user.setEmail("client@test.com");

        ClientProfile profile = new ClientProfile();
        profile.setId(2L);
        profile.setUser(user);
        profile.setCompanyName("Tech Corp");
        profile.setBio("We build future");
        profile.setTotalSpent(new BigDecimal("1000.00"));
        profile.setRating(new BigDecimal("5.0"));

        ClientProfileResponse response = clientProfileMapper.toResponse(profile);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUserId()).isEqualTo(88L);
        assertThat(response.getEmail()).isEqualTo("client@test.com");
        assertThat(response.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(response.getTotalSpent()).isEqualTo(new BigDecimal("1000.00"));
    }
}