package org.propflow.back.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import net.jsreport.java.JsReportException;
import net.jsreport.java.service.JsReportService;
import net.jsreport.java.service.JsReportServiceImpl;

@Service
public class ReportService {

    String fileName = "proposal.pdf";
    final String TEMPLATE = "proposalTemplate";
    final String pdfPATH = "back/src/main/resources/static/pdf/";
    JsReportService reportingService = new JsReportServiceImpl("http://localhost:5488");

    // Initialize folder to save pdf rendered:
    public void init() {
        try {
            Files.createDirectories(Paths.get(pdfPATH));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder to save Pdf´s!");
        }
    }

    // Empty pdf folder:
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(pdfPATH).toFile());
    }

    /**
     * Creates the file from a map with the proposal and calculations
     * @param dataMap
     * @return pdf file path
     */
    @SuppressWarnings("rawtypes")
    public String create(Map<String, Object> data){
        CompletableFuture.supplyAsync(() -> {
            try {
                // Access to data Map:
                HashMap proposalObject  = (HashMap) data.get("proposal");
                HashMap candidateObject = (HashMap) proposalObject.get("candidate");
                // Create file name:
                fileName = proposalObject.get("startDate") + "_" + candidateObject.get("name") + ".pdf";
                return this.reportingService.render(TEMPLATE, data);
            } catch (JsReportException ex) {
                throw new CompletionException(ex);
            }
        }).thenAcceptAsync(report -> {
            try {
                Files.copy(report.getContent(), Paths.get(pdfPATH+fileName), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("pdf file created in backend and path returned");
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
        return pdfPATH+fileName;
    }

    // STILL NOT IMPLEMENTED, DOWNLOADING IN FRONT AT THE MOMENT
    public Resource download(Map<String, Object> data){

        String filePath = this.create(data);
        try {
            Resource resource = new UrlResource(filePath);

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

}
