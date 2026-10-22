package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.ExtraTemplate;
import org.propflow.back.service.ExtraTemplateService;
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
@RequestMapping("/v1/api/extratemplates")
@Tag(name = "extra-templates", description = "Extra templates")
public class ExtraTemplateController {

  @Autowired private ExtraTemplateService extraTemplateService;

  @Operation(summary = "List all extra templates")
  @GetMapping({"", "/"})
  public List<ExtraTemplate> all() {
    return this.extraTemplateService.all();
  }

  @Operation(summary = "List extra templates paginated")
  @GetMapping(
      value = {"", "/"},
      params = {"page", "size", "!filter"})
  public ResponseEntity<Map<String, Object>> all(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "3") int size) {
    log.info("Accessing paginated ExtraTemplates");
    log.info("PAGE: " + page + " & SIZE: " + size);

    Map<String, Object> responseAll = this.extraTemplateService.all(page, size);
    return ResponseEntity.ok(responseAll);
  }

  @Operation(summary = "Find extra templates by name")
  @GetMapping(
      value = {"", "/"},
      params = {"!page", "!size", "filter"})
  public List<ExtraTemplate> filterByName(@RequestParam String filter) {
    return this.extraTemplateService.findByName(filter);
  }

  @Operation(summary = "Get an extra template by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Extra template found"),
    @ApiResponse(responseCode = "500", description = "Extra template not found or internal error")
  })
  @GetMapping("/{id}")
  public ExtraTemplate one(@PathVariable("id") Long id) {
    return this.extraTemplateService.one(id);
  }

  @Operation(summary = "Create an extra template")
  @ApiResponse(responseCode = "200", description = "Extra template created")
  @PostMapping({"", "/"})
  public ExtraTemplate newExtraTemplate(@RequestBody ExtraTemplate extra) {
    log.info("Creating new extra = " + extra);
    return this.extraTemplateService.save(extra);
  }

  @Operation(summary = "Replace an extra template")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Extra template replaced"),
    @ApiResponse(responseCode = "500", description = "Extra template not found or internal error")
  })
  @PutMapping("/{id}")
  public ExtraTemplate replaceExtraTemplate(
      @PathVariable("id") Long id, @RequestBody ExtraTemplate extra) {
    log.info("Updated extra with id = " + id + "\n Extra = " + extra);
    return this.extraTemplateService.replace(id, extra);
  }

  @Operation(summary = "Delete an extra template")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Extra template deleted"),
    @ApiResponse(responseCode = "500", description = "Extra template not found or internal error")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteExtra(@PathVariable("id") Long id) {
    log.info("Deleting extra with id = " + id);
    this.extraTemplateService.delete(id);
  }
}
