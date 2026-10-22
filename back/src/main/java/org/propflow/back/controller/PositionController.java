package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.dto.PositionDTO;
import org.propflow.back.mapper.PositionMapper;
import org.propflow.back.model.Position;
import org.propflow.back.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/positions")
@Tag(name = "positions", description = "Position management")
public class PositionController {

  @Autowired private PositionService positionService;
  @Autowired private PositionMapper positionMapper;

  @Operation(summary = "List all positions")
  @GetMapping({"", "/"})
  public List<PositionDTO> all() {
    return positionService.all();
  }

  @Operation(summary = "List positions paginated")
  @GetMapping(
      value = {"", "/"},
      params = {"page", "size"})
  public ResponseEntity<Map<String, Object>> allPaginated(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "3") int size) {
    log.info("Accessing paginated Positions");
    log.info("PAGE: " + page + " & SIZE: " + size);

    Map<String, Object> responseAll = positionService.all(page, size);
    return ResponseEntity.ok(responseAll);
  }

  @Operation(summary = "Get a position by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Position found"),
    @ApiResponse(responseCode = "404", description = "Position not found")
  })
  @GetMapping("/{id}")
  public PositionDTO one(@PathVariable("id") Long id) {
    return positionMapper.toDTO(positionService.one(id));
  }

  @Operation(summary = "Create a position")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Position created"),
    @ApiResponse(responseCode = "400", description = "Invalid position payload")
  })
  @PostMapping({"", "/"})
  public PositionDTO newPosition(@Valid @RequestBody Position position) {
    log.info("Creating new position = " + position);
    return positionMapper.toDTO(positionService.save(position));
  }

  @Operation(summary = "Replace a position")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Position replaced"),
    @ApiResponse(responseCode = "400", description = "Invalid position payload"),
    @ApiResponse(responseCode = "404", description = "Position not found"),
    @ApiResponse(responseCode = "409", description = "Path id does not match position id")
  })
  @PutMapping("/{id}")
  public PositionDTO replacePosition(
      @PathVariable("id") Long id, @Valid @RequestBody Position position) {
    log.info("Updated position with id = " + id + "\n Position = " + position);
    return positionMapper.toDTO(positionService.replace(id, position));
  }

  @Operation(summary = "Delete a position")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Position deleted"),
    @ApiResponse(responseCode = "404", description = "Position not found")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deletePosition(@PathVariable("id") Long id) {
    log.info("Deleting position with id = " + id);
    positionService.delete(id);
  }
}
