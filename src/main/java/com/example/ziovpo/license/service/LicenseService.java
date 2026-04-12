package com.example.ziovpo.license.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.ziovpo.license.dto.ActivateLicenseRequest;
import com.example.ziovpo.license.dto.CheckLicenseRequest;
import com.example.ziovpo.license.dto.CreateLicenseRequest;
import com.example.ziovpo.license.dto.LicenseHistoryResponse;
import com.example.ziovpo.license.dto.LicenseResponse;
import com.example.ziovpo.license.dto.RenewLicenseRequest;
import com.example.ziovpo.license.dto.Ticket;
import com.example.ziovpo.license.dto.TicketResponse;
import com.example.ziovpo.license.model.LicenseDevice;
import com.example.ziovpo.license.model.LicenseDeviceLicense;
import com.example.ziovpo.license.model.LicenseLicense;
import com.example.ziovpo.license.model.LicenseLicenseHistory;
import com.example.ziovpo.license.model.LicenseLicenseType;
import com.example.ziovpo.license.model.LicenseProduct;
import com.example.ziovpo.license.repository.LicenseDeviceLicenseRepository;
import com.example.ziovpo.license.repository.LicenseDeviceRepository;
import com.example.ziovpo.license.repository.LicenseHistoryRepository;
import com.example.ziovpo.license.repository.LicenseRepository;
import com.example.ziovpo.model.Users;

@Service
public class LicenseService {

	private final LicenseProductService licenseProductService;
	private final LicenseTypeService licenseTypeService;
	private final LicenseApplicationUserService licenseApplicationUserService;
	private final LicenseRepository licenseRepository;
	private final LicenseHistoryRepository licenseHistoryRepository;
	private final LicenseDeviceRepository licenseDeviceRepository;
	private final LicenseDeviceLicenseRepository licenseDeviceLicenseRepository;
	private final TicketSignatureService ticketSignatureService;
	private final CodeGenerator codeGenerator;

	public LicenseService(
			LicenseProductService licenseProductService,
			LicenseTypeService licenseTypeService,
			LicenseApplicationUserService licenseApplicationUserService,
			LicenseRepository licenseRepository,
			LicenseHistoryRepository licenseHistoryRepository,
			LicenseDeviceRepository licenseDeviceRepository,
			LicenseDeviceLicenseRepository licenseDeviceLicenseRepository,
			TicketSignatureService ticketSignatureService,
			CodeGenerator codeGenerator
	) {
		this.licenseProductService = licenseProductService;
		this.licenseTypeService = licenseTypeService;
		this.licenseApplicationUserService = licenseApplicationUserService;
		this.licenseRepository = licenseRepository;
		this.licenseHistoryRepository = licenseHistoryRepository;
		this.licenseDeviceRepository = licenseDeviceRepository;
		this.licenseDeviceLicenseRepository = licenseDeviceLicenseRepository;
		this.ticketSignatureService = ticketSignatureService;
		this.codeGenerator = codeGenerator;
	}

	@Transactional
	public TicketResponse activateLicense(ActivateLicenseRequest request, UUID userId) {
		if (request.getActivationKey() == null || request.getActivationKey().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activationKey is required");
		}
		if (request.getDeviceMac() == null || request.getDeviceMac().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceMac is required");
		}

		Users currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);

		LicenseLicense license = licenseRepository.findByCode(request.getActivationKey())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

		if (license.getUser() != null && !license.getUser().getId().equals(currentUser.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "license owned by another user");
		}

		LicenseDevice device = licenseDeviceRepository.findByMacAddress(request.getDeviceMac())
				.orElseGet(() -> {
					LicenseDevice newDevice = new LicenseDevice();
					newDevice.setMacAddress(request.getDeviceMac());
					newDevice.setName(request.getDeviceName() != null ? request.getDeviceName() : request.getDeviceMac());
					newDevice.setUser(currentUser);
					return licenseDeviceRepository.save(newDevice);
				});

		if (!device.getUser().getId().equals(currentUser.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "device owned by another user");
		}

		boolean firstActivation = license.getUser() == null;

		if (firstActivation) {
			license.setUser(currentUser);
			license.setFirstActivationDate(LocalDate.now());
			int days = license.getType().getDefaultDurationInDays();
			license.setEndingDate(license.getFirstActivationDate().plusDays(days));
			licenseRepository.save(license);

			LicenseDeviceLicense dll = new LicenseDeviceLicense();
			dll.setLicense(license);
			dll.setDevice(device);
			licenseDeviceLicenseRepository.save(dll);

			LicenseLicenseHistory history = new LicenseLicenseHistory();
			history.setLicense(license);
			history.setUser(currentUser);
			history.setStatus("ACTIVATED");
			history.setChangeDate(LocalDate.now());
			history.setDescription("First activation");
			licenseHistoryRepository.save(history);

			Ticket ticket = buildTicket(license, device);
			String signature = ticketSignatureService.signTicket(ticket);
			return new TicketResponse(ticket, signature);
		}

		boolean exists = licenseDeviceLicenseRepository.existsByLicense_IdAndDevice_Id(license.getId(), device.getId());
		if (!exists) {
			int limit = license.getDeviceCount();
			if (limit > 0) {
				long current = licenseDeviceLicenseRepository.countByLicense_Id(license.getId());
				if (current >= limit) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "device limit reached");
				}
			}

			LicenseDeviceLicense dll = new LicenseDeviceLicense();
			dll.setLicense(license);
			dll.setDevice(device);
			licenseDeviceLicenseRepository.save(dll);

			LicenseLicenseHistory history = new LicenseLicenseHistory();
			history.setLicense(license);
			history.setUser(currentUser);
			history.setStatus("ACTIVATED");
			history.setChangeDate(LocalDate.now());
			history.setDescription("Device activation");
			licenseHistoryRepository.save(history);
		}

		Ticket ticket = buildTicket(license, device);
		String signature = ticketSignatureService.signTicket(ticket);
		return new TicketResponse(ticket, signature);
	}

	@Transactional(readOnly = true)
	public TicketResponse checkLicense(CheckLicenseRequest request, UUID userId) {
		if (request.getDeviceMac() == null || request.getDeviceMac().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceMac is required");
		}
		if (request.getProductId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
		}

		Users currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);
		LocalDate today = LocalDate.now();

		LicenseDevice device = licenseDeviceRepository.findByMacAddress(request.getDeviceMac())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "device not found"));

		LicenseLicense license = licenseRepository.findActiveByDeviceUserAndProduct(
						device.getId(),
						currentUser.getId(),
						request.getProductId(),
						today)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

		Ticket ticket = buildTicket(license, device);
		String signature = ticketSignatureService.signTicket(ticket);
		return new TicketResponse(ticket, signature);
	}

	@Transactional
	public LicenseResponse createLicense(CreateLicenseRequest request, UUID adminId) {
		if (request.getProductId() == null || request.getTypeId() == null || request.getOwnerId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId, typeId, ownerId are required");
		}
		if (request.getDeviceCount() != null && request.getDeviceCount() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceCount must be >= 0");
		}

		LicenseProduct product = licenseProductService.getProductOrFail(request.getProductId());
		LicenseLicenseType type = licenseTypeService.getTypeOrFail(request.getTypeId());
		Users owner = licenseApplicationUserService.getActiveUserOrFail(request.getOwnerId());
		Users admin = licenseApplicationUserService.getActiveUserOrFail(adminId);

		LicenseLicense license = createNewLicense(request, product, type, owner);
		LicenseLicense savedLicense = licenseRepository.save(license);

		LicenseLicenseHistory history = new LicenseLicenseHistory();
		history.setLicense(savedLicense);
		history.setUser(admin);
		history.setStatus("CREATED");
		history.setChangeDate(LocalDate.now());
		history.setDescription("License created");
		licenseHistoryRepository.save(history);

		return toResponse(savedLicense);
	}

	@Transactional(readOnly = true)
	public List<LicenseHistoryResponse> getLicenseHistory(UUID licenseId, UUID userId) {
		Users currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);

		LicenseLicense license = licenseRepository.findById(licenseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

		boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
		boolean isOwner = license.getOwner() != null && license.getOwner().getId().equals(currentUser.getId());
		boolean isCurrentUser = license.getUser() != null && license.getUser().getId().equals(currentUser.getId());
		if (!isAdmin && !isOwner && !isCurrentUser) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
		}

		return licenseHistoryRepository.findByLicense_IdOrderByChangeDateDescIdDesc(licenseId)
				.stream()
				.map(this::toHistoryResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LicenseResponse> getAllLicenses(UUID adminId) {
		Users admin = licenseApplicationUserService.getActiveUserOrFail(adminId);
		requireAdmin(admin);

		return licenseRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public LicenseResponse getLicenseByCode(String code, UUID adminId) {
		Users admin = licenseApplicationUserService.getActiveUserOrFail(adminId);
		requireAdmin(admin);

		LicenseLicense license = licenseRepository.findByCode(code)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));
		return toResponse(license);
	}

	@Transactional
	public TicketResponse renewLicense(RenewLicenseRequest request, UUID userId) {
		if (request.getActivationKey() == null || request.getActivationKey().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activationKey is required");
		}

		Users currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);
		LicenseLicense license = licenseRepository.findByCode(request.getActivationKey())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

		if (license.isBlocked()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "license is blocked");
		}

		LocalDate today = LocalDate.now();
		LocalDate endDate = license.getEndingDate();
		boolean inactive = license.getUser() == null || endDate == null;
		boolean expiringSoon = endDate != null && !endDate.isAfter(today.plusDays(7));
		if (!inactive && !expiringSoon) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "license renewal is not allowed (expires > 7 days from now)");
		}
		LocalDate baseDate = (endDate != null && endDate.isAfter(today)) ? endDate : today;

		int durationDays = license.getType().getDefaultDurationInDays();
		LocalDate newEndDate = baseDate.plusDays(durationDays);

		license.setEndingDate(newEndDate);
		licenseRepository.save(license);

		LicenseLicenseHistory history = new LicenseLicenseHistory();
		history.setLicense(license);
		history.setUser(currentUser);
		history.setStatus("RENEWED");
		history.setChangeDate(LocalDate.now());
		history.setDescription("License renewed until " + newEndDate);
		licenseHistoryRepository.save(history);

		Ticket ticket = buildTicket(license, null);
		String signature = ticketSignatureService.signTicket(ticket);
		return new TicketResponse(ticket, signature);
	}

	private LicenseLicense createNewLicense(
			CreateLicenseRequest request,
			LicenseProduct product,
			LicenseLicenseType type,
			Users owner
	) {
		LicenseLicense license = new LicenseLicense();
		license.setCode(generateUniqueCode());
		license.setProduct(product);
		license.setType(type);
		license.setOwner(owner);
		license.setUser(null);
		license.setFirstActivationDate(null);
		license.setEndingDate(null);
		license.setBlocked(false);
		Integer requestedDeviceCount = request.getDeviceCount();
		license.setDeviceCount(requestedDeviceCount == null ? 0 : requestedDeviceCount);
		license.setDescription(request.getDescription());
		return license;
	}

	private String generateUniqueCode() {
		for (int i = 0; i < 20; i++) {
			String code = codeGenerator.generateCode();
			if (licenseRepository.findByCode(code).isEmpty()) {
				return code;
			}
		}
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to generate code");
	}

	private LicenseResponse toResponse(LicenseLicense license) {
		LicenseResponse resp = new LicenseResponse();
		resp.setId(license.getId());
		resp.setCode(license.getCode());
		resp.setProductId(license.getProduct().getId());
		resp.setTypeId(license.getType().getId());
		resp.setOwnerId(license.getOwner().getId());
		resp.setUserId(license.getUser() != null ? license.getUser().getId() : null);
		resp.setFirstActivationDate(license.getFirstActivationDate());
		resp.setEndingDate(license.getEndingDate());
		resp.setBlocked(license.isBlocked());
		resp.setDeviceCount(license.getDeviceCount());
		resp.setDescription(license.getDescription());
		return resp;
	}

	private LicenseHistoryResponse toHistoryResponse(LicenseLicenseHistory history) {
		return new LicenseHistoryResponse(
				history.getId(),
				history.getLicense() != null ? history.getLicense().getId() : null,
				history.getUser() != null ? history.getUser().getId() : null,
				history.getStatus(),
				history.getChangeDate(),
				history.getDescription());
	}

	private Ticket buildTicket(LicenseLicense license, LicenseDevice device) {
		Ticket ticket = new Ticket();
		ticket.setServerTime(LocalDateTime.now());
		ticket.setTicketLifetimeSeconds(3600);
		ticket.setFirstActivationDate(license.getFirstActivationDate());
		ticket.setExpirationDate(license.getEndingDate());
		ticket.setUserId(license.getUser() != null ? license.getUser().getId() : null);
		ticket.setDeviceId(device != null ? device.getId() : null);
		ticket.setBlocked(license.isBlocked());
		return ticket;
	}

	private void requireAdmin(Users user) {
		if (!"ROLE_ADMIN".equals(user.getRole())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
		}
	}
}
