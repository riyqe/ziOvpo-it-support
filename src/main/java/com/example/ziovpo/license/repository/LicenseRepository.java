package com.example.ziovpo.license.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ziovpo.license.model.LicenseLicense;

public interface LicenseRepository extends JpaRepository<LicenseLicense, UUID> {
	Optional<LicenseLicense> findByCode(String code);

	@Query("""
			select l
			from LicenseLicense l
			join LicenseDeviceLicense dl on dl.license = l
			join dl.device d
			where d.id = :deviceId
			  and l.user.id = :userId
			  and l.product.id = :productId
			  and l.blocked = false
			  and l.endingDate >= :today
		""")
	Optional<LicenseLicense> findActiveByDeviceUserAndProduct(
			@Param("deviceId") UUID deviceId,
			@Param("userId") UUID userId,
			@Param("productId") UUID productId,
			@Param("today") LocalDate today
	);
}

