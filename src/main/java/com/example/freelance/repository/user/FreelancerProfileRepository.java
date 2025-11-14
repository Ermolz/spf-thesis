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

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT DISTINCT f FROM FreelancerProfile f " +
           "WHERE (:minRating IS NULL OR f.rating >= :minRating) " +
           "AND (:maxRating IS NULL OR f.rating <= :maxRating) " +
           "AND (:minHourlyRate IS NULL OR f.hourlyRate >= :minHourlyRate) " +
           "AND (:maxHourlyRate IS NULL OR f.hourlyRate <= :maxHourlyRate) " +
           "AND (:currency IS NULL OR f.currency = :currency) " +
           "AND (:skill IS NULL OR :skill MEMBER OF f.skills)")
    Page<FreelancerProfile> searchFreelancers(
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating,
            @Param("minHourlyRate") BigDecimal minHourlyRate,
            @Param("maxHourlyRate") BigDecimal maxHourlyRate,
            @Param("currency") String currency,
            @Param("skill") String skill,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT DISTINCT f FROM FreelancerProfile f " +
           "WHERE (:minRating IS NULL OR f.rating >= :minRating) " +
           "AND (:maxRating IS NULL OR f.rating <= :maxRating) " +
           "AND (:minHourlyRate IS NULL OR f.hourlyRate >= :minHourlyRate) " +
           "AND (:maxHourlyRate IS NULL OR f.hourlyRate <= :maxHourlyRate) " +
           "AND (:currency IS NULL OR f.currency = :currency) " +
           "AND (:skills IS NULL OR EXISTS (SELECT s FROM f.skills s WHERE s IN :skills))")
    Page<FreelancerProfile> searchFreelancersWithMultipleSkills(
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating,
            @Param("minHourlyRate") BigDecimal minHourlyRate,
            @Param("maxHourlyRate") BigDecimal maxHourlyRate,
            @Param("currency") String currency,
            @Param("skills") List<String> skills,
            Pageable pageable
    );
}

