package org.propflow.back.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.propflow.back.dto.PositionDTO;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.mapper.PositionMapper;
import org.propflow.back.model.Position;
import org.propflow.back.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

  @Autowired private PositionRepository positionRepository;
  @Autowired private PositionMapper positionMapper;

  public List<PositionDTO> all() {
    return positionRepository.findAll().stream()
        .map(positionMapper::toDTO)
        .collect(Collectors.toList());
  }

  public Map<String, Object> all(int page, int size) {
    Pageable paginator = PageRequest.of(page, size, Sort.by("id").ascending());
    Page<Position> pageAll = positionRepository.findAll(paginator);

    Map<String, Object> response = new HashMap<>();
    response.put(
        "positions",
        pageAll.getContent().stream().map(positionMapper::toDTO).collect(Collectors.toList()));
    response.put("currentPage", pageAll.getNumber());
    response.put("totalItems", pageAll.getTotalElements());
    response.put("totalPages", pageAll.getTotalPages());
    return response;
  }

  public Position save(Position position) {
    return positionRepository.save(position);
  }

  public Position one(Long id) {
    return positionRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, Position.class));
  }

  public Position replace(Long id, Position position) {
    return positionRepository
        .findById(id)
        .map(
            existing -> {
              if (id.equals(position.getId())) {
                return positionRepository.save(position);
              } else {
                throw new NotCouplingIdException(id, position.getId(), Position.class);
              }
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Position.class));
  }

  @Transactional
  public void delete(Long id) {
    positionRepository
        .findById(id)
        .map(
            existing -> {
              positionRepository.unlinkCandidates(existing);
              positionRepository.unlinkProposals(existing);
              positionRepository.delete(existing);
              return existing;
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Position.class));
  }
}
