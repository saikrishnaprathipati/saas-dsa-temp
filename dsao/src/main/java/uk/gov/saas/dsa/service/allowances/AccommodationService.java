package uk.gov.saas.dsa.service.allowances;

import static uk.gov.saas.dsa.service.allowances.AllowancesServiceUtil.toAccommodationVO;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationStudAccommodation;
import uk.gov.saas.dsa.model.AccommodationType;
import uk.gov.saas.dsa.persistence.DSAApplicationAccommodationRepository;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

@Service
public class AccommodationService {

	private final DSAApplicationAccommodationRepository repo;

	public AccommodationService(DSAApplicationAccommodationRepository repo) {
		this.repo = repo;
	}

	public AccommodationVO getAccommodation(long id) throws IllegalAccessException {
		AccommodationVO accommodationVO;
		Optional<DSAApplicationStudAccommodation> entity = repo.findById(id);
		if (entity.isPresent() && !entity.isEmpty()) {
			DSAApplicationStudAccommodation accommodation = entity.get();
			accommodationVO = toAccommodationVO(accommodation);
		} else {
			throw new IllegalAccessException("");
		}
		return accommodationVO;
	}

	public List<AccommodationVO> getAccommodations(long dsaApplicationNumber) {
		return AllowancesServiceUtil.getAccommodations(getAllAccommodationBy(dsaApplicationNumber));
	}

	public AccommodationVO addAccommodationType(AccommodationVO vo) {
		DSAApplicationStudAccommodation entity = new DSAApplicationStudAccommodation();

		deleteEmptyRecordsIfExists(vo.getDsaApplicationNumber());

		entity.setDsaApplicationNumber(vo.getDsaApplicationNumber());
		entity.setStudentReferenceNumber(vo.getStudentReferenceNumber());
		entity.setAccommodationType(vo.getAccommodationType());

		entity.setCreatedBy(LoggedinUserUtil.getUserId());
		entity.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
		entity.setLastUpdatedBy(entity.getCreatedBy());
		entity.setLastUpdatedDate(entity.getCreatedDate());

		DSAApplicationStudAccommodation accommodation = repo.save(entity);
		AccommodationVO accommodationVO = toAccommodationVO(accommodation);
		return accommodationVO;
	}

	private void deleteEmptyRecordsIfExists(long dsaApplicationNumber) {
		List<DSAApplicationStudAccommodation> list = getAllAccommodationBy(dsaApplicationNumber);
		List<Long> emptyList = filterAccommodationsWithEmptyData(list);
		if (emptyList.size() > 0) {
			repo.deleteAllById(emptyList);
		}
	}

	private List<Long> filterAccommodationsWithEmptyData(List<DSAApplicationStudAccommodation> list) {
		return list.stream()
				.filter(t -> t.getWeeks() == 0 && t.getEnhancedCost() == null && t.getStandardCost() == null)
				.map(DSAApplicationStudAccommodation::getId).collect(Collectors.toList());
	}

	public void updateAccommodation(AccommodationVO vo) {
		Optional<DSAApplicationStudAccommodation> accommodation = repo.findById(vo.getId());
		DSAApplicationStudAccommodation studAccommodation = accommodation.get();

		studAccommodation.setEnhancedCost(vo.getEnhancedCost());
		studAccommodation.setStandardCost(vo.getStandardCost());
		studAccommodation.setWeeks(vo.getWeeks());

		studAccommodation.setLastUpdatedBy(LoggedinUserUtil.getUserId());
		studAccommodation.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		repo.save(studAccommodation);
	}

	public void deleteAccommodation(long id) {
		repo.deleteById(id);
	}

	private List<DSAApplicationStudAccommodation> getAllAccommodationBy(long dsaApplicationNumber) {
		List<DSAApplicationStudAccommodation> list = repo.findByDsaApplicationNumber(dsaApplicationNumber);
		return (list != null && !list.isEmpty()) ? list : new ArrayList<DSAApplicationStudAccommodation>();
	}

	public AccommodationVO findAccommodationType(@Valid AccommodationVO formVO) {

		long dsaApplicationNumber = formVO.getDsaApplicationNumber();
		AccommodationType accommodationType = formVO.getAccommodationType();

		DSAApplicationStudAccommodation entity = repo
				.findByDsaApplicationNumberAndAccommodationTypeAndEnhancedCostIsNullAndStandardCostIsNull(
						dsaApplicationNumber, accommodationType);
		AccommodationVO accommodationVO = toAccommodationVO(entity);
		return accommodationVO;

	}

}
