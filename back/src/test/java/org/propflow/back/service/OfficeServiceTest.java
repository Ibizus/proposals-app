package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Office;
import org.propflow.back.repository.OfficeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class OfficeServiceTest {

  @Mock private OfficeRepository officeRepository;

  @InjectMocks private OfficeService officeService;

  private static Office office(long id, String name, String code) {
    return new Office(id, name, code, new HashSet<>());
  }

  @Test
  void oneReturnsOfficeWhenFound() {
    Office lisbon = office(1, "Lisbon", "LIS");
    when(officeRepository.findById(1L)).thenReturn(Optional.of(lisbon));

    assertThat(officeService.one(1L)).isSameAs(lisbon);
  }

  @Test
  void oneThrowsWhenMissing() {
    when(officeRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> officeService.one(9L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Office")
        .hasMessageContaining("9");
  }

  @Test
  void filterSearchesNameAndCodeWithFirstPageSortedByName() {
    Office malaga = office(2, "Malaga", "ESP-MAL");
    when(officeRepository.findOfficesByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            eq("mal"), eq("mal"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(malaga)));

    Page<Office> result = officeService.filter(Optional.of("mal"));

    assertThat(result.getContent()).containsExactly(malaga);
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(officeRepository)
        .findOfficesByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            eq("mal"), eq("mal"), pageable.capture());
    assertThat(pageable.getValue().getPageNumber()).isZero();
    assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
    assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
  }

  @Test
  void filterReturnsNullWithoutWord() {
    assertThat(officeService.filter(Optional.empty())).isNull();
  }

  @Test
  void replaceSavesWhenIdsMatch() {
    Office updated = office(1, "Lisbon HQ", "LIS");
    when(officeRepository.findById(1L)).thenReturn(Optional.of(office(1, "Lisbon", "LIS")));
    when(officeRepository.save(updated)).thenReturn(updated);

    assertThat(officeService.replace(1L, updated)).isSameAs(updated);
  }

  @Test
  void replaceRejectsMismatchedIds() {
    when(officeRepository.findById(1L)).thenReturn(Optional.of(office(1, "Lisbon", "LIS")));

    assertThatThrownBy(() -> officeService.replace(1L, office(2, "Other", "OTH")))
        .isInstanceOf(NotCouplingIdException.class);
    verify(officeRepository, never()).save(any());
  }

  @Test
  void replaceThrowsWhenMissing() {
    when(officeRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> officeService.replace(1L, office(1, "Lisbon", "LIS")))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void deleteRemovesExistingOffice() {
    Office lisbon = office(1, "Lisbon", "LIS");
    when(officeRepository.findById(1L)).thenReturn(Optional.of(lisbon));

    officeService.delete(1L);

    verify(officeRepository).delete(lisbon);
  }

  @Test
  void deleteThrowsWhenMissing() {
    when(officeRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> officeService.delete(1L)).isInstanceOf(EntityNotFoundException.class);
    verify(officeRepository, never()).delete(any());
  }
}
