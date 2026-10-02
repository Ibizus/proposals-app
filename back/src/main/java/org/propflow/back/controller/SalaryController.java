package org.propflow.back.controller;

import java.util.List;

import org.propflow.back.model.Salary;
import org.propflow.back.service.SalaryService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/salaries")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    @GetMapping({"", "/"})
    public List<Salary> all() {
        return salaryService.all();
    }

    @GetMapping( "/{id}")
    public Salary one(@PathVariable("id") Long id) {
        return salaryService.one(id);
    }

    @PostMapping({"","/"})
    public Salary newSalary(@RequestBody Salary salary) {
        log.info("Creating new salary = " + salary);
        return this.salaryService.save(salary);
    }

    @PutMapping("/{id}")
    public Salary replaceSalary(@PathVariable("id") Long id, @RequestBody Salary salary) {
        log.info("Updated salary with id = " + id + "\n Salary = " + salary);
        return this.salaryService.replace(id, salary);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteSalary(@PathVariable("id") Long id) {
        log.info("Deleting salary with id = " + id);
        this.salaryService.delete(id);
    }
}
