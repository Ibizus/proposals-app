package org.propflow.back;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.propflow.back.model.Candidate;
import org.propflow.back.model.ExtraTemplate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Parameter;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Status;
import org.propflow.back.model.Taxes;
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
import org.propflow.back.service.SalaryService;
import org.propflow.back.service.TaxesService;
import org.springframework.beans.factory.annotation.Autowired;

public class InsertTest {
  @Autowired
  OfficeService officeService;
  @Autowired
  ProposalService proposalService;
  @Autowired
  CandidateService candidateService;
  @Autowired
  SalaryService salaryService;
  @Autowired
  ExtraService extraService;
  @Autowired
  ExtraTemplateService extraTemplateService;
  @Autowired
  TaxesService taxesService;
  @Autowired
  ParameterService parameterService;

  private CandidateRepository candidateRepository;
  @SuppressWarnings("unused")
  private ExtraRepository extraRepository;
  private OfficeRepository officeRepository;
  private ProposalRepository proposalRepository;
  private SalaryRepository salaryRepository;
  private StatusRepository statusRepository;

  ExtraTemplate eT1;
  ExtraTemplate eT2;
  ExtraTemplate eT3;
  ExtraTemplate eT4;
  ExtraTemplate eT5;
  ExtraTemplate eT6;
  ExtraTemplate eT7;
  ExtraTemplate eT8;
  ExtraTemplate eT9;

  public InsertTest(OfficeService officeService, ProposalService proposalService, CandidateService candidateService,
      SalaryService salaryService, ExtraService extraService, ExtraTemplateService extraTemplateService,
      TaxesService taxesService, ParameterService parameterService, CandidateRepository candidateRepository,
      ExtraRepository extraRepository,
      OfficeRepository officeRepository, ProposalRepository proposalRepository, SalaryRepository salaryRepository,
      StatusRepository statusRepository) {
    this.officeService = officeService;
    this.proposalService = proposalService;
    this.candidateService = candidateService;
    this.salaryService = salaryService;
    this.extraService = extraService;
    this.extraTemplateService = extraTemplateService;
    this.taxesService = taxesService;
    this.parameterService = parameterService;
    this.candidateRepository = candidateRepository;
    this.extraRepository = extraRepository;
    this.officeRepository = officeRepository;
    this.proposalRepository = proposalRepository;
    this.salaryRepository = salaryRepository;
    this.statusRepository = statusRepository;
  }

  public void candidateInsert() {
    // Candidate c1 = new Candidate(0, "John", "Doe", "123456789", "691909035",
    // "jdoe@example.com", "0", "Not married", 0,
    // new HashSet<>(), new HashSet<>());
    // candidateRepository.save(c1);

    // Candidate c2 = new Candidate(0, "Marcus", "Brown", "987654321", "691357923",
    // "mbrown@example.com", "57891",
    // "Married, 1 working", 1, new HashSet<>(), new HashSet<>());
    // candidateRepository.save(c2);

    // Candidate c3 = new Candidate(0, "Daniele", "Longley", "787263617",
    // "793456127", "dlongley@example.com", "98271",
    // "Married, 2 working", 3, new HashSet<>(), new HashSet<>());
    // candidateRepository.save(c3);
  }

  public void proposalInsert() {
    Proposal p1 = new Proposal(0, "nJoyn1", "Proposal ipsum dolor sit amet", "Some role", null, null,
        LocalDate.of(2028, 10, 31),
        null,
        "The more you know", new Office(), new Salary(), new Candidate(), new HashSet<Status>());

    proposalRepository.save(p1);

    Proposal p2 = new Proposal(0, "nJoyn2", "Second Proposal ipsum dolor sit amet", "Second some role", null, null,
        LocalDate.of(2030, 7, 20),
        null, "The more you know (The second)", new Office(), new Salary(), new Candidate(), new HashSet<Status>());

    proposalRepository.save(p2);
  }

  public void extraInsert() {
    // Extra e1 = new Extra(0, "Car", "This is the first extra", LocalDate.of(2030,
    // 1, 12), (double) 203, new Salary());
    // extraRepository.save(e1);

    // Extra e2 = new Extra(0, "Phone", "This is the first phone extra",
    // LocalDate.of(2024, 8, 1), (double) 50,
    // new Salary());
    // extraRepository.save(e2);
  }

  public void officeInsert() {
    Office o1 = new Office(0, "Oriente", "ORI-LIS", new HashSet<>());
    officeRepository.save(o1);

    Office o2 = new Office(0, "Malaga", "ESP-MAL", new HashSet<>());
    officeRepository.save(o2);
  }

  public void salaryInsert() {
    Salary s1 = new Salary(0, (double) 1190, new Proposal(), new HashSet<>());
    salaryRepository.save(s1);

    Salary s2 = new Salary(0, (double) 1300, new Proposal(), new HashSet<>());
    salaryRepository.save(s2);
  }

  public void insertAllData() throws InterruptedException {
    int numberOfProposals = 200; // Adjust number as needed
    for (int i = 0; i < numberOfProposals; i++) {
      // Instantiate Offices
      Office office = new Office(0, "Office " + i, "OFF-" + i, new HashSet<>());

      // Instantiate Salaries
      double baseSalary = 1000.0 + i * 50.0; // Adjust salary values as needed
      Salary salary = new Salary(0, baseSalary, null, new HashSet<>());

      // Instantiate Candidates
      Candidate candidate = new Candidate(0, "Candidate" + i, "Lastname" + i, "ID-" + i, "Phone-" + i,
          "email" + i + "@example.com", "Address-" + i, i % 3, i % 6, 1, new HashSet<>(), new HashSet<>());
      candidateRepository.save(candidate);

      int day = (int) (Math.random() * 27) + 1;
      LocalDate date = LocalDate.of(2024, 4, day);

      // Create new Status instances for each Proposal
      Set<Status> newStatuses = new HashSet<>();
      Status newStatus1 = new Status(0, 0, "a" + i, null);
      Thread.sleep(10); // Sleep for 10 milliseconds
      Status newStatus2 = new Status(0, 1, "b" + i, null);
      Thread.sleep(10); // Sleep for 10 milliseconds
      Status newStatus3 = new Status(0, 2, "c" + i, null);
      Thread.sleep(10); // Sleep for 10 milliseconds
      Status newStatus4 = new Status(0, 3, "d" + i, null);
      Thread.sleep(10); // Sleep for 10 milliseconds
      Status newStatus5 = new Status(0, 4, "e" + i, null);
      Thread.sleep(10); // Sleep for 10 milliseconds
      Status newStatus6 = new Status(0, 5, "f" + i, null);

      // Instantiate Proposals and associate with Offices and Salaries
      Proposal proposal = new Proposal(0, "nJoyn" + i, "Proposal " + i, "Role " + i, null, null,
          date, null, "Additional info " + i, office, salary, candidate, newStatuses);

      // Add the status to the proposal
      switch (i % 6) {
        case 0:
          newStatus1.setProposal(proposal);
          newStatuses.add(newStatus1);
          break;
        case 1:
          newStatus2.setProposal(proposal);
          newStatuses.add(newStatus2);
          break;
        case 2:
          newStatus3.setProposal(proposal);
          newStatuses.add(newStatus3);
          break;
        case 3:
          newStatus4.setProposal(proposal);
          newStatuses.add(newStatus4);
          break;
        case 4:
          newStatus5.setProposal(proposal);
          newStatuses.add(newStatus5);
          break;
        case 5:
          newStatus6.setProposal(proposal);
          newStatuses.add(newStatus6);
          break;
        default:
          break;
      }

      proposal.setStatuses(newStatuses);

      // Associate Proposals with Offices and Salaries
      office.getProposals().add(proposal);
      salary.setProposal(proposal);

      // Save all entities to the database
      officeRepository.save(office);
      salaryRepository.save(salary);
      proposal.setCreatedAt(date.atStartOfDay());
      proposalRepository.save(proposal);

      statusRepository.save(newStatus1);
      statusRepository.save(newStatus2);
      statusRepository.save(newStatus3);
      statusRepository.save(newStatus4);
      statusRepository.save(newStatus5);
      statusRepository.save(newStatus6);
    }

    // Extra template fields corrected:
    eT1 = new ExtraTemplate(0, "Flex", 4d, 11d, 21d, true, false, false, false, false, 12, -1);
    eT2 = new ExtraTemplate(0, "Car Allowance", 250d, 11d, 21d, false, true, true, false, true, 12, 1);
    eT3 = new ExtraTemplate(0, "Car", 350d, 11d, 21d, false, false, false, false, false, 12, 1);
    eT4 = new ExtraTemplate(0, "Fuel Allowance", 100d, 11d, 21d, false, false, false, false, true, 12, 1);
    eT5 = new ExtraTemplate(0, "Food Allowance", 15d, 11d, 21d, false, false, false, false, true, 11, 21); // Amount per
                                                                                                           // day
    eT6 = new ExtraTemplate(0, "Phone", 0d, 11d, 21d, false, false, false, true, false, 12, 1);
    eT7 = new ExtraTemplate(0, "Computer", 0d, 11d, 21d, false, false, false, true, false, 12, 1);
    eT8 = new ExtraTemplate(0, "Health Insurance", 0d, 11d, 21d, false, true, false, false, false, 12, 1);
    eT9 = new ExtraTemplate(0, "Life Insurance", 0d, 11d, 21d, false, true, false, false, false, 12, 1);

    extraTemplateService.save(eT1);
    extraTemplateService.save(eT2);
    extraTemplateService.save(eT3);
    extraTemplateService.save(eT4);
    extraTemplateService.save(eT5);
    extraTemplateService.save(eT6);
    extraTemplateService.save(eT7);
    extraTemplateService.save(eT8);
    extraTemplateService.save(eT9);

    // Salary Ranges and Tax Percentages for illustrative purposes, replace with
    // actual data
    Double[] salaryMaxes = { 762d, 766d, 787d, 851d, 964d, 1051d, 1113d, 1194d, 1280d, 1380d, 1466d, 1609d, 1762d,
        1925d, 2035d, 2151d, 2283d, 2437d, 2609d, 2848d, 3195d, 3637d, 4239d, 4786d, 5346d, 6052d, 6924d, 8171d, 9840d,
        11612d, 19404d, 20811d, 23413d, 26014d, 9999999d };
    // Unmarried Tax Rates
    Double[][] unmarriedRates = {
        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
        { 2.5, 0.4, 0.0, 0.0, 0.0, 0.0 },
        { 4.9, 0.7, 0.0, 0.0, 0.0, 0.0 },
        { 7.8, 4.4, 0.9, 0.0, 0.0, 0.0 },
        { 10.0, 6.6, 3.4, 0.0, 0.0, 0.0 },
        { 11.2, 7.8, 5.6, 1.3, 0.0, 0.0 },
        { 12.0, 8.7, 6.4, 3.2, 0.0, 0.0 },
        { 13.0, 10.6, 8.2, 5.0, 2.6, 0.1 },
        { 14.0, 11.7, 9.2, 6.0, 3.5, 1.1 },
        { 15.1, 12.7, 10.4, 6.9, 4.5, 2.1 },
        { 16.2, 13.8, 11.4, 8.0, 6.5, 4.0 },
        { 17.2, 14.8, 12.3, 10.0, 7.5, 5.0 },
        { 18.6, 16.3, 14.8, 11.4, 8.9, 6.5 },
        { 19.9, 18.2, 17.3, 14.5, 12.5, 11.7 },
        { 20.9, 19.3, 18.2, 15.5, 14.5, 12.5 },
        { 21.9, 20.2, 19.2, 16.4, 15.5, 13.5 },
        { 22.8, 21.3, 20.3, 17.5, 16.5, 14.5 },
        { 23.8, 22.2, 21.3, 18.5, 17.6, 15.5 },
        { 24.8, 24.2, 22.2, 20.4, 18.5, 17.6 },
        { 25.8, 25.1, 23.3, 21.4, 19.4, 18.5 },
        { 27.0, 26.4, 24.5, 22.5, 20.6, 19.6 },
        { 28.6, 28.3, 26.8, 25.2, 24.6, 23.0 },
        { 29.7, 29.5, 27.7, 26.2, 25.6, 25.0 },
        { 31.4, 31.0, 29.4, 27.6, 27.0, 26.5 },
        { 32.3, 31.8, 31.3, 28.9, 28.0, 27.4 },
        { 33.3, 32.8, 32.2, 29.8, 29.2, 28.4 },
        { 35.3, 34.9, 34.1, 32.2, 31.8, 31.5 },
        { 36.3, 35.9, 35.5, 34.2, 32.8, 32.4 },
        { 38.2, 37.8, 37.4, 36.2, 35.8, 34.4 },
        { 39.2, 38.8, 38.4, 37.5, 36.7, 35.4 },
        { 40.2, 39.8, 39.4, 38.5, 38.1, 36.4 },
        { 41.2, 40.8, 40.4, 39.5, 39.1, 37.3 },
        { 41.9, 41.7, 41.1, 40.5, 40.1, 38.5 },
        { 42.9, 42.7, 42.3, 41.4, 41.1, 39.7 },
        { 43.8, 43.6, 43.3, 42.4, 42.0, 40.7 }
    };

    // Married One Income Tax Rates
    Double[][] marriedOneIncomeRates = {
        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
        { 3.2, 0.0, 0.0, 0.0, 0.0, 0.0 },
        { 3.2, 0.0, 0.0, 0.0, 0.0, 0.0 },
        { 4.6, 0.8, 0.0, 0.0, 0.0, 0.0 },
        { 5.5, 1.7, 0.8, 0.0, 0.0, 0.0 },
        { 6.4, 3.6, 1.1, 0.0, 0.0, 0.0 },
        { 7.2, 4.5, 2.8, 0.0, 0.0, 0.0 },
        { 8.0, 5.4, 3.6, 0.9, 0.0, 0.0 },
        { 9.1, 6.8, 4.6, 1.9, 0.0, 0.0 },
        { 10.7, 8.9, 7.1, 4.3, 2.6, 1.7 },
        { 11.7, 10.0, 8.1, 6.3, 4.5, 2.7 },
        { 13.1, 11.4, 10.6, 7.7, 5.9, 5.1 },
        { 14.0, 12.4, 11.7, 9.0, 7.3, 6.5 },
        { 15.0, 13.3, 12.5, 10.0, 9.2, 7.4 },
        { 16.0, 14.3, 13.5, 10.9, 10.2, 8.5 },
        { 17.0, 16.3, 14.6, 11.9, 11.1, 9.5 },
        { 17.8, 17.2, 15.6, 13.8, 12.1, 11.4 },
        { 18.9, 18.2, 16.6, 14.8, 13.1, 12.3 },
        { 21.5, 21.4, 19.8, 18.4, 17.1, 16.7 },
        { 22.4, 22.3, 21.0, 19.4, 19.0, 17.6 },
        { 23.4, 23.3, 22.0, 20.6, 20.0, 18.6 },
        { 24.4, 24.3, 22.9, 21.6, 21.2, 20.6 },
        { 25.9, 25.3, 23.9, 22.5, 22.1, 21.8 },
        { 26.9, 26.3, 25.9, 23.5, 23.1, 22.7 },
        { 27.8, 27.2, 26.9, 24.5, 24.1, 23.7 },
        { 28.8, 28.2, 27.8, 25.5, 25.1, 24.7 },
        { 29.7, 29.6, 29.2, 27.0, 26.9, 26.7 },
        { 30.7, 30.6, 30.4, 29.0, 27.8, 27.6 },
        { 32.1, 32.0, 31.9, 30.7, 29.3, 29.1 },
        { 33.1, 33.0, 32.8, 31.7, 31.5, 30.0 },
        { 34.4, 34.3, 33.8, 32.6, 32.4, 31.3 },
        { 36.4, 36.3, 36.2, 35.1, 34.9, 33.7 },
        { 37.3, 37.2, 37.1, 36.5, 35.9, 34.7 },
        { 38.3, 38.2, 38.1, 37.4, 37.2, 35.7 },
        { 39.3, 39.2, 39.1, 38.4, 38.4, 37.0 },
    };

    // Married Two Incomes Tax Rates
    Double[][] marriedTwoIncomesRates = {
        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
        { 2.5, 1.8, 0.4, 0.0, 0.0, 0.0 },
        { 4.9, 3.6, 0.9, 0.0, 0.0, 0.0 },
        { 7.8, 5.0, 3.3, 2.4, 0.5, 0.0 },
        { 10.0, 7.2, 6.4, 3.7, 3.0, 1.1 },
        { 11.2, 8.5, 7.7, 5.0, 4.3, 3.0 },
        { 12.0, 9.4, 8.5, 5.9, 4.7, 3.8 },
        { 13.0, 11.3, 10.5, 7.8, 7.0, 5.2 },
        { 14.0, 12.3, 11.4, 8.8, 7.9, 6.2 },
        { 15.1, 14.3, 12.5, 10.6, 8.9, 8.0 },
        { 16.2, 15.3, 13.6, 11.9, 10.0, 9.2 },
        { 17.2, 16.4, 14.6, 12.8, 11.1, 10.2 },
        { 18.6, 17.7, 16.1, 14.3, 13.4, 11.7 },
        { 19.9, 19.3, 17.6, 16.0, 15.2, 13.5 },
        { 20.9, 20.4, 18.5, 16.9, 16.1, 14.5 },
        { 21.9, 21.4, 19.6, 17.7, 17.0, 16.3 },
        { 22.8, 22.3, 20.7, 18.9, 17.9, 17.3 },
        { 23.8, 23.4, 22.6, 19.9, 19.1, 18.2 },
        { 24.8, 24.4, 23.6, 21.0, 20.2, 19.4 },
        { 25.7, 25.2, 24.6, 21.9, 21.2, 20.4 },
        { 26.9, 26.5, 25.7, 23.1, 22.3, 21.6 },
        { 28.5, 28.4, 28.0, 25.7, 25.3, 24.9 },
        { 29.6, 29.5, 29.0, 27.6, 26.3, 25.9 },
        { 31.4, 31.1, 30.7, 29.0, 27.7, 27.3 },
        { 32.3, 32.0, 31.6, 30.3, 29.6, 28.3 },
        { 33.3, 33.0, 32.6, 31.3, 30.9, 29.2 },
        { 35.2, 35.0, 34.5, 33.8, 33.6, 33.4 },
        { 36.2, 36.0, 35.8, 34.7, 34.6, 34.4 },
        { 38.1, 37.9, 37.7, 36.6, 36.4, 36.3 },
        { 39.1, 38.9, 38.7, 38.0, 37.4, 37.2 },
        { 40.1, 39.8, 39.7, 39.0, 38.8, 38.2 },
        { 41.1, 40.9, 40.7, 40.0, 39.8, 39.2 },
        { 41.8, 41.7, 41.6, 41.0, 40.8, 40.4 },
        { 42.8, 42.7, 42.6, 41.9, 41.7, 41.5 },
        { 43.8, 43.7, 43.6, 42.9, 42.7, 42.5 }
    };

    LocalDate validFrom = LocalDate.of(2024, 1, 1);
    LocalDate validUntil = LocalDate.of(2024, 12, 31);

    for (int i = 0; i < salaryMaxes.length; i++) {
      Taxes taxUnmarried = new Taxes(0, salaryMaxes[i], 11d, 23.75, validFrom, validUntil, 0,
          unmarriedRates[i][0], unmarriedRates[i][1], unmarriedRates[i][2],
          unmarriedRates[i][3], unmarriedRates[i][4], unmarriedRates[i][5]);
      Taxes taxMarried1 = new Taxes(0, salaryMaxes[i], 11d, 23.75, validFrom, validUntil, 1,
          marriedOneIncomeRates[i][0], marriedOneIncomeRates[i][1], marriedOneIncomeRates[i][2],
          marriedOneIncomeRates[i][3], marriedOneIncomeRates[i][4], marriedOneIncomeRates[i][5]);

      Taxes taxMarried2 = new Taxes(0, salaryMaxes[i], 11d, 23.75, validFrom, validUntil, 2,
          marriedTwoIncomesRates[i][0], marriedTwoIncomesRates[i][1], marriedTwoIncomesRates[i][2],
          marriedTwoIncomesRates[i][3], marriedTwoIncomesRates[i][4], marriedTwoIncomesRates[i][5]);

      taxesService.save(taxUnmarried);
      taxesService.save(taxMarried1);
      taxesService.save(taxMarried2);
    }

    Parameter param1 = new Parameter(0, "iht", 20.0, null, null);
    Parameter param2 = new Parameter(0, "spp", 3.0, null, null);
    Parameter param3 = new Parameter(0, "scr", 0.9235, null, null);

    parameterService.save(param1);
    parameterService.save(param2);
    parameterService.save(param3);
  }

  // public void removeData(){
  // Optional<Salary> salary = salaryRepository.findByPhoneId((long)1);
  // if (salary.isPresent()) {
  // salaryRepository.save(salary.get());
  // }

  // officeRepository.deleteById((long)1);
  // salaryRepository.deleteById((long)1);
  // proposalRepository.deleteById((long)1);
  // extraRepository.deleteById((long)1);
  // candidateRepository.deleteById((long)1);

  // }

}
