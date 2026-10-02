package org.propflow.back.controller;

import java.util.Map;
import com.google.gson.Gson;
import org.propflow.back.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
//@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/api/pdf")
public class ReportController {

    @Autowired 
    private ReportService reportService;
    private static final Gson gson = new Gson();

    @PostMapping(value = {"create","create/"})
    public ResponseEntity<String> createPDF(@RequestBody Map<String, Object> data){

        log.info("Data received in backend to create PDF");
        String fileName = this.reportService.create(data);

        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(fileName));
    }


    @PostMapping(value = {"download","download/"})
    @ResponseBody
    public ResponseEntity<Resource> downloadPDF(@RequestBody Map<String, Object> data){

        log.info("Data received in backend to download PDF");
        Resource file = this.reportService.download(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

}
