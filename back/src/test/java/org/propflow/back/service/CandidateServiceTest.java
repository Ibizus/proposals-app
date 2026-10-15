package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.model.Candidate;
import org.propflow.back.repository.CandidateRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

  @Mock
  private CandidateRepository candidateRepository;

  @InjectMocks
  private CandidateService candidateService;

  private static Candidate candidate(long id, String name, String idNumber) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    candidate.setName(name);
    candidate.setIdNumber(idNumber);
    return candidate;
  }

  @Test
  void saveReusesExistingCandidateWithSameIdNumber() {
    Candidate existing = candidate(7, "Existing", "ID-1");
    Candidate incoming = candidate(0, "Incoming", "ID-1");
    when(candidateRepository.findCandidateByIdNumber("ID-1")).thenReturn(existing);
    when(candidateRepository.save(existing)).thenReturn(existing);

    assertThat(candidateService.save(incoming)).isSameAs(existing);
    verify(candidateRepository).save(existing);
  }

  @Test
  void saveStoresNewCandidateWhenIdNumberIsUnknown() {
    Candidate incoming = candidate(0, "New", "ID-2");
    when(candidateRepository.findCandidateByIdNumber("ID-2")).thenReturn(null);
    when(candidateRepository.save(incoming)).thenReturn(incoming);

    assertThat(candidateService.save(incoming)).isSameAs(incoming);
  }

  @Test
  void paginatedListExposesPageMetadata() {
    List<Candidate> content = List.of(candidate(4, "D", "ID-4"), candidate(5, "E", "ID-5"));
    Pageable request = PageRequest.of(1, 3, Sort.by("id").ascending());
    when(candidateRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(content, request, 5));

    Map<String, Object> response = candidateService.all(1, 3);

    assertThat(response)
        .containsEntry("candidates", content)
        .containsEntry("currentPage", 1)
        .containsEntry("totalItems", 5L)
        .containsEntry("totalPages", 2);
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(candidateRepository).findAll(pageable.capture());
    assertThat(pageable.getValue()).isEqualTo(request);
  }

  @Test
  void filterAppliesSameTermToEverySearchableField() {
    when(candidateRepository
        .findCandidatesByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrIdNumberContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrEmailContainingIgnoreCaseOrWorkerIdContainingIgnoreCase(
            eq("ann"), eq("ann"), eq("ann"), eq("ann"), eq("ann"), eq("ann"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(candidate(1, "Ann", "ID-1"))));

    Map<String, Object> response = candidateService.findByFilter(0, 10, "ann");

    assertThat(response.get("totalItems")).isEqualTo(1L);
  }
}
