package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.dto.PositionDTO;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.mapper.PositionMapper;
import org.propflow.back.model.Position;
import org.propflow.back.repository.PositionRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

  @Mock private PositionRepository positionRepository;
  @Mock private PositionMapper positionMapper;

  @InjectMocks private PositionService positionService;

  private static Position position(long id, String title) {
    return new Position(id, title, "description", "NJ-" + id, "more", new HashSet<>());
  }

  private static PositionDTO dto(long id, String title) {
    return new PositionDTO(
        id, title, "description", "NJ-" + id, "more", Set.of(), null, null, null, null);
  }

  @Test
  void allMapsEveryPositionToDTO() {
    Position backend = position(1, "Backend");
    when(positionRepository.findAll()).thenReturn(List.of(backend));
    when(positionMapper.toDTO(backend)).thenReturn(dto(1, "Backend"));

    List<PositionDTO> result = positionService.all();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).title()).isEqualTo("Backend");
  }

  @Test
  void paginatedListExposesPageMetadata() {
    Position backend = position(1, "Backend");
    Position frontend = position(2, "Frontend");
    Pageable request = PageRequest.of(1, 3, Sort.by("id").ascending());
    when(positionRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(backend, frontend), request, 5));
    when(positionMapper.toDTO(backend)).thenReturn(dto(1, "Backend"));
    when(positionMapper.toDTO(frontend)).thenReturn(dto(2, "Frontend"));

    Map<String, Object> response = positionService.all(1, 3);

    assertThat((List<?>) response.get("positions")).hasSize(2);
    assertThat(response)
        .containsEntry("currentPage", 1)
        .containsEntry("totalItems", 5L)
        .containsEntry("totalPages", 2);
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(positionRepository).findAll(pageable.capture());
    assertThat(pageable.getValue()).isEqualTo(request);
  }

  @Test
  void saveDelegatesToRepository() {
    Position backend = position(0, "Backend");
    when(positionRepository.save(backend)).thenReturn(backend);

    assertThat(positionService.save(backend)).isSameAs(backend);
  }

  @Test
  void oneReturnsPositionWhenFound() {
    Position backend = position(1, "Backend");
    when(positionRepository.findById(1L)).thenReturn(Optional.of(backend));

    assertThat(positionService.one(1L)).isSameAs(backend);
  }

  @Test
  void oneThrowsWhenMissing() {
    when(positionRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> positionService.one(9L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Position")
        .hasMessageContaining("9");
  }

  @Test
  void replaceSavesWhenIdsMatch() {
    Position updated = position(1, "Backend");
    when(positionRepository.findById(1L)).thenReturn(Optional.of(position(1, "Backend")));
    when(positionRepository.save(updated)).thenReturn(updated);

    assertThat(positionService.replace(1L, updated)).isSameAs(updated);
  }

  @Test
  void replaceRejectsMismatchedIds() {
    when(positionRepository.findById(1L)).thenReturn(Optional.of(position(1, "Backend")));

    assertThatThrownBy(() -> positionService.replace(1L, position(2, "Other")))
        .isInstanceOf(NotCouplingIdException.class);
    verify(positionRepository, never()).save(any());
  }

  @Test
  void replaceThrowsWhenMissing() {
    when(positionRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> positionService.replace(1L, position(1, "Backend")))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void deleteRemovesExistingPosition() {
    Position backend = position(1, "Backend");
    when(positionRepository.findById(1L)).thenReturn(Optional.of(backend));

    positionService.delete(1L);

    verify(positionRepository).delete(backend);
  }

  @Test
  void deleteThrowsWhenMissing() {
    when(positionRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> positionService.delete(1L))
        .isInstanceOf(EntityNotFoundException.class);
    verify(positionRepository, never()).delete(any());
  }
}
