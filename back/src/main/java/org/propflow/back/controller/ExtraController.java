package org.propflow.back.controller;

import java.util.List;

import org.propflow.back.model.Extra;
import org.propflow.back.service.ExtraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/extras")
public class ExtraController {

    @Autowired
    private ExtraService extraService;

    @GetMapping({"", "/"})
    public List<Extra> all() {
        return this.extraService.all();
    }

    @GetMapping(value = {"", "/"}, params = "filter")
    public List<Extra> filterByNameAndDescription(@RequestParam String filter) {
        return this.extraService.findByNameAndDescription(filter);
    }

    @GetMapping( "/{id}")
    public Extra one(@PathVariable("id") Long id) {
        return this.extraService.one(id);
    }

    @PostMapping({"","/"})
    public Extra newExtra(@RequestBody Extra extra) {
        log.info("Creating new extra = " + extra);
        return this.extraService.save(extra);
    }

    @PutMapping("/{id}")
    public Extra replaceExtra(@PathVariable("id") Long id, @RequestBody Extra extra) {
        log.info("Updated extra with id = " + id + "\n Extra = " + extra);
        return this.extraService.replace(id, extra);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteExtra(@PathVariable("id") Long id) {
        log.info("Deleting extra with id = " + id);
        this.extraService.delete(id);
    }
}
