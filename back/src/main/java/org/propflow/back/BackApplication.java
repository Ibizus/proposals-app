package org.propflow.back;

import java.util.TimeZone;
import org.propflow.back.repository.CandidateRepository;
import org.propflow.back.repository.ExtraRepository;
import org.propflow.back.repository.OfficeRepository;
import org.propflow.back.repository.ProposalRepository;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.repository.StatusRepository;
import org.propflow.back.service.CandidateService;
import org.propflow.back.service.ExtraService;
import org.propflow.back.service.ExtraTemplateService;
import org.propflow.back.service.OfficeService;
import org.propflow.back.service.ParameterService;
import org.propflow.back.service.ProposalService;
import org.propflow.back.service.ReportService;
import org.propflow.back.service.SalaryService;
import org.propflow.back.service.TaxesService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackApplication.class, args);
  }

  // Seeds demo data on startup; tests set app.seed-demo-data=false to skip it.
  @Bean
  @ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
  public CommandLineRunner run(
      OfficeService officeService,
      ProposalService proposalService,
      CandidateService candidateService,
      ReportService reportService,
      SalaryService salaryService,
      ExtraService extraService,
      ExtraTemplateService extraTemplateService,
      TaxesService taxesService,
      ParameterService parameterService,
      CandidateRepository candidateRepository,
      ExtraRepository extraRepository,
      OfficeRepository officeRepository,
      ProposalRepository proposalRepository,
      SalaryRepository salaryRepository,
      StatusRepository statusRepository) {
    return (args) -> {
      InsertTest it =
          new InsertTest(
              officeService,
              proposalService,
              candidateService,
              salaryService,
              extraService,
              extraTemplateService,
              taxesService,
              parameterService,
              candidateRepository,
              extraRepository,
              officeRepository,
              proposalRepository,
              salaryRepository,
              statusRepository);

      TimeZone.setDefault(TimeZone.getTimeZone("USA/Eastern"));
      // it.candidateInsert();
      // it.proposalInsert();
      // it.extraInsert();
      // it.officeInsert();
      // it.phoneInsert();
      // it.salaryInsert();

      // Delete any item in PDF folder if exists:
      reportService.deleteAll();
      // Create PDF folder:
      reportService.init();
      it.insertAllData();

      // it.removeData();
    };
  }
}
