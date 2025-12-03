package com.example.freelance.repository.user;

import com.example.freelance.domain.user.FreelancerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, Long> {
    Optional<FreelancerProfile> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "skills"})
    @Query("SELECT DISTINCT f FROM FreelancerProfile f " +
           "WHERE f.rating >= COALESCE(:minRating, f.rating) " +
           "AND f.rating <= COALESCE(:maxRating, f.rating) " +
           "AND f.hourlyRate >= COALESCE(:minHourlyRate, f.hourlyRate) " +
           "AND f.hourlyRate <= COALESCE(:maxHourlyRate, f.hourlyRate) " +
           "AND (:currency IS NULL OR f.currency = :currency) " +
           "AND (:skillNames IS NULL OR EXISTS (SELECT s FROM f.skills s WHERE s.name IN :skillNames))")
    Page<FreelancerProfile> searchFreelancers(
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating,
            @Param("minHourlyRate") BigDecimal minHourlyRate,
            @Param("maxHourlyRate") BigDecimal maxHourlyRate,
            @Param("currency") String currency,
            @Param("skillNames") List<String> skillNames,
            Pageable pageable
    );
}

