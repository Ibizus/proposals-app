package org.propflow.back.seed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.propflow.back.model.AuditableSuperclass;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Extra;
import org.propflow.back.model.ExtraTemplate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Parameter;
import org.propflow.back.model.Position;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Status;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxBracket;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.propflow.back.repository.CandidateRepository;
import org.propflow.back.repository.ExtraRepository;
import org.propflow.back.repository.ExtraTemplateRepository;
import org.propflow.back.repository.OfficeRepository;
import org.propflow.back.repository.ParameterRepository;
import org.propflow.back.repository.PositionRepository;
import org.propflow.back.repository.ProposalRepository;
import org.propflow.back.repository.StatusRepository;
import org.propflow.back.repository.TaxConfigurationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fills an empty development database with fictitious demo data.
 *
 * <p>Runs only when {@code app.seed-demo-data=true} (set by the {@code dev} profile) and only fills
 * tables that are still empty, so restarting the app never duplicates rows. Datafaker uses a fixed
 * seed and all dates are fixed, so every fresh database gets exactly the same data.
 */
@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

  static final int POSITION_COUNT = 5;
  static final int PROPOSAL_COUNT = 16;
  static final int CANDIDATE_COUNT = 24;

  private static final long FAKER_SEED = 20260101L;
  private static final LocalDateTime FIRST_PROPOSAL_AT = LocalDateTime.of(2026, 5, 4, 9, 0);
  private static final LocalDateTime FIRST_POSITION_AT = FIRST_PROPOSAL_AT.minusDays(10);

  private static final String[][] OFFICES = {
    {"Lisbon Oriente", "ORI-LIS"},
    {"Porto", "PRT-OPO"},
    {"Braga", "PRT-BRG"},
    {"Malaga", "ESP-MAL"},
    {"Madrid", "ESP-MAD"},
    {"Munich", "DEU-MUC"}
  };
  private static final String[][] POSITIONS = {
    {
      "Java Backend Developer",
      "Design and maintain the backend services that power the platform.",
      "NJ-POS-01",
      "https://example.com/positions/java-backend"
    },
    {
      "Angular Frontend Developer",
      "Build and maintain the Angular frontend used by recruiters.",
      "NJ-POS-02",
      "https://example.com/positions/angular-frontend"
    },
    {
      "Full Stack Developer",
      "Work across backend and frontend to ship end-to-end features.",
      "NJ-POS-03",
      "https://example.com/positions/full-stack"
    },
    {
      "DevOps Engineer",
      "Own the CI/CD pipeline and the cloud infrastructure.",
      "NJ-POS-04",
      "https://example.com/positions/devops"
    },
    {
      "QA Automation Engineer",
      "Automate the test suite and keep the quality gates green.",
      "NJ-POS-05",
      "https://example.com/positions/qa-automation"
    }
  };
  private static final String[] CONTRACT_LEVELS = {"L1", "L2", "L3", "L4", "L5", "L6", "L7"};

  // Status codes as shown in the frontend: 0 Draft, 1 Confirmed, 2 Sent, 3 Declined,
  // 4 Cancelled, 5 Approved. Each history ends in the proposal's current status.
  private static final int[][] STATUS_HISTORIES = {
    {0}, {0, 1}, {0, 1, 2}, {0, 1, 2}, {0, 1, 2, 5}, {0, 1, 2, 5}, {0, 1, 2, 3}, {0, 4}, {0, 1, 4}
  };
  private static final String[] STATUS_MESSAGES = {
    "Draft created",
    "Confirmed by the hiring manager",
    "Offer sent to the candidate",
    "Candidate declined the offer",
    "Proposal cancelled",
    "Candidate accepted the offer"
  };

  private final OfficeRepository officeRepository;
  private final CandidateRepository candidateRepository;
  private final ProposalRepository proposalRepository;
  private final PositionRepository positionRepository;
  private final StatusRepository statusRepository;
  private final ExtraRepository extraRepository;
  private final ExtraTemplateRepository extraTemplateRepository;
  private final TaxConfigurationRepository taxConfigurationRepository;
  private final ParameterRepository parameterRepository;

  private TaxConfiguration spainConfiguration;
  private TaxConfiguration portugalConfiguration;
  private long spainSocialSecurityId;
  private long spainIncomeTaxId;
  private long portugalSocialSecurityId;
  private long portugalIncomeTaxId;

  public DemoDataSeeder(
      OfficeRepository officeRepository,
      CandidateRepository candidateRepository,
      ProposalRepository proposalRepository,
      PositionRepository positionRepository,
      StatusRepository statusRepository,
      ExtraRepository extraRepository,
      ExtraTemplateRepository extraTemplateRepository,
      TaxConfigurationRepository taxConfigurationRepository,
      ParameterRepository parameterRepository) {
    this.officeRepository = officeRepository;
    this.candidateRepository = candidateRepository;
    this.proposalRepository = proposalRepository;
    this.positionRepository = positionRepository;
    this.statusRepository = statusRepository;
    this.extraRepository = extraRepository;
    this.extraTemplateRepository = extraTemplateRepository;
    this.taxConfigurationRepository = taxConfigurationRepository;
    this.parameterRepository = parameterRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    seed();
  }

  /** Seeds every group of tables that is still empty. */
  public void seed() {
    seedTaxConfigurations();
    seedIfEmpty("extra templates", extraTemplateRepository, this::seedExtraTemplates);
    seedIfEmpty("salary parameters", parameterRepository, this::seedParameters);
    if (officeRepository.count() == 0
        && positionRepository.count() == 0
        && candidateRepository.count() == 0
        && proposalRepository.count() == 0) {
      seedDemoData();
      log.info(
          "Seeded demo data: {} offices, {} positions, {} candidates, {} proposals",
          OFFICES.length,
          POSITION_COUNT,
          CANDIDATE_COUNT,
          PROPOSAL_COUNT);
    } else {
      log.info(
          "Offices, positions, candidates or proposals already exist; skipping demo proposals");
    }
  }

  private void seedIfEmpty(String label, JpaRepository<?, Long> repository, Runnable seeder) {
    if (repository.count() == 0) {
      seeder.run();
      log.info("Seeded {} {}", repository.count(), label);
    } else {
      log.info("Table for {} is not empty; skipping", label);
    }
  }

  private void seedTaxConfigurations() {
    spainConfiguration =
        taxConfigurationRepository
            .findByCountryCode("es")
            .orElseGet(() -> taxConfigurationRepository.save(buildSpainConfiguration()));
    portugalConfiguration =
        taxConfigurationRepository
            .findByCountryCode("pt")
            .orElseGet(() -> taxConfigurationRepository.save(buildPortugalConfiguration()));

    // Ensure exactly one configuration is active: Spain by default.
    if (taxConfigurationRepository.findByActiveTrue().isEmpty()) {
      spainConfiguration.setActive(true);
      portugalConfiguration.setActive(false);
      taxConfigurationRepository.saveAll(List.of(spainConfiguration, portugalConfiguration));
    }

    spainSocialSecurityId = taxId(spainConfiguration, "Social Security");
    spainIncomeTaxId = taxId(spainConfiguration, "IRPF");
    portugalSocialSecurityId = taxId(portugalConfiguration, "Social Security");
    portugalIncomeTaxId = taxId(portugalConfiguration, "IRS");
  }

  private long taxId(TaxConfiguration configuration, String name) {
    return configuration.getTaxes().stream()
        .filter(t -> name.equals(t.getName()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    name + " tax not found in " + configuration.getCountryCode()))
        .getId();
  }

  private TaxConfiguration buildSpainConfiguration() {
    TaxConfiguration config = new TaxConfiguration();
    config.setCountryCode("es");
    config.setLabel("Spain (illustrative example data)");
    config.setYearlyPayments(12);
    config.setActive(true);

    Tax socialSecurity = new Tax();
    socialSecurity.setName("Social Security");
    socialSecurity.setType(TaxType.FIXED);
    socialSecurity.setFixedRate(6.45);
    socialSecurity.setConfiguration(config);

    Tax irpf = new Tax();
    irpf.setName("IRPF");
    irpf.setType(TaxType.BRACKETED);
    irpf.setConfiguration(config);
    irpf.setBrackets(spainIrpfBrackets(irpf));

    config.setTaxes(List.of(socialSecurity, irpf));
    return config;
  }

  private List<TaxBracket> spainIrpfBrackets(Tax irpf) {
    return List.of(
        bracket(irpf, 1200, 0.0),
        bracket(irpf, 1500, 8.0),
        bracket(irpf, 2000, 12.0),
        bracket(irpf, 3000, 17.0),
        bracket(irpf, 4000, 24.0),
        bracket(irpf, 6000, 30.0),
        bracket(irpf, 9999999, 37.0));
  }

  private TaxConfiguration buildPortugalConfiguration() {
    TaxConfiguration config = new TaxConfiguration();
    config.setCountryCode("pt");
    config.setLabel("Portugal (illustrative example data)");
    config.setYearlyPayments(14);
    config.setActive(false);

    Tax socialSecurity = new Tax();
    socialSecurity.setName("Social Security");
    socialSecurity.setType(TaxType.FIXED);
    socialSecurity.setFixedRate(11.0);
    socialSecurity.setConfiguration(config);

    Tax irs = new Tax();
    irs.setName("IRS");
    irs.setType(TaxType.WITHHOLDING);
    irs.setConfiguration(config);
    irs.setBrackets(portugalIrsBrackets(irs));

    config.setTaxes(List.of(socialSecurity, irs));
    return config;
  }

  private List<TaxBracket> portugalIrsBrackets(Tax irs) {
    List<TaxBracket> brackets = new ArrayList<>();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                new ClassPathResource("seed/taxes.csv").getInputStream(),
                StandardCharsets.UTF_8))) {
      reader
          .lines()
          .filter(line -> !line.isBlank() && !line.startsWith("#") && !line.startsWith("marital"))
          .map(line -> line.split(","))
          .filter(c -> "0".equals(c[0]))
          .forEach(c -> brackets.add(bracket(irs, Double.valueOf(c[1]), Double.valueOf(c[2]))));
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read seed/taxes.csv", e);
    }
    return brackets;
  }

  private TaxBracket bracket(Tax tax, double threshold, double rate) {
    TaxBracket bracket = new TaxBracket();
    bracket.setThreshold(threshold);
    bracket.setRate(rate);
    bracket.setTax(tax);
    return bracket;
  }

  private void seedExtraTemplates() {
    extraTemplateRepository.saveAll(
        Stream.concat(spainTemplates().stream(), portugalTemplates().stream()).toList());
  }

  private List<ExtraTemplate> spainTemplates() {
    return List.of(
        template(
            "Flex",
            4d,
            true,
            false,
            false,
            12,
            -1,
            spainConfiguration.getId(),
            Set.of(spainSocialSecurityId)),
        template(
            "Car Allowance",
            250d,
            false,
            false,
            true,
            12,
            1,
            spainConfiguration.getId(),
            Set.of(spainIncomeTaxId),
            true),
        template("Car", 350d, false, false, false, 12, 1, spainConfiguration.getId(), Set.of()),
        template(
            "Fuel Allowance",
            100d,
            false,
            false,
            false,
            12,
            1,
            spainConfiguration.getId(),
            Set.of(),
            true),
        template(
            "Food Allowance",
            15d,
            false,
            false,
            false,
            11,
            21,
            spainConfiguration.getId(),
            Set.of(spainIncomeTaxId)),
        template("Phone", 0d, false, false, false, 12, 1, spainConfiguration.getId(), Set.of()),
        template("Computer", 0d, false, false, false, 12, 1, spainConfiguration.getId(), Set.of()),
        template(
            "Health Insurance",
            0d,
            false,
            true,
            false,
            12,
            1,
            spainConfiguration.getId(),
            Set.of(spainIncomeTaxId)),
        template(
            "Life Insurance",
            0d,
            false,
            true,
            false,
            12,
            1,
            spainConfiguration.getId(),
            Set.of(spainIncomeTaxId)));
  }

  private List<ExtraTemplate> portugalTemplates() {
    return List.of(
        template(
            "Flex",
            4d,
            true,
            false,
            false,
            12,
            -1,
            portugalConfiguration.getId(),
            Set.of(portugalSocialSecurityId)),
        template(
            "Car Allowance",
            250d,
            false,
            false,
            true,
            12,
            1,
            portugalConfiguration.getId(),
            Set.of(portugalIncomeTaxId),
            true),
        template("Car", 350d, false, false, false, 12, 1, portugalConfiguration.getId(), Set.of()),
        template(
            "Fuel Allowance",
            100d,
            false,
            false,
            false,
            12,
            1,
            portugalConfiguration.getId(),
            Set.of(),
            true),
        template(
            "Food Allowance",
            15d,
            false,
            false,
            false,
            11,
            21,
            portugalConfiguration.getId(),
            Set.of(portugalIncomeTaxId)),
        template("Phone", 0d, false, false, false, 12, 1, portugalConfiguration.getId(), Set.of()),
        template(
            "Computer", 0d, false, false, false, 12, 1, portugalConfiguration.getId(), Set.of()),
        template(
            "Health Insurance",
            0d,
            false,
            true,
            false,
            12,
            1,
            portugalConfiguration.getId(),
            Set.of(portugalIncomeTaxId)),
        template(
            "Life Insurance",
            0d,
            false,
            true,
            false,
            12,
            1,
            portugalConfiguration.getId(),
            Set.of(portugalIncomeTaxId)));
  }

  private ExtraTemplate template(
      String name,
      double amount,
      boolean isPercentage,
      boolean isDefault,
      boolean addToSalary,
      int multiplyYearly,
      int multiplyMonthly,
      Long taxConfigurationId,
      Set<Long> taxIds) {
    return template(
        name,
        amount,
        isPercentage,
        isDefault,
        addToSalary,
        multiplyYearly,
        multiplyMonthly,
        taxConfigurationId,
        taxIds,
        false);
  }

  private ExtraTemplate template(
      String name,
      double amount,
      boolean isPercentage,
      boolean isDefault,
      boolean addToSalary,
      int multiplyYearly,
      int multiplyMonthly,
      Long taxConfigurationId,
      Set<Long> taxIds,
      boolean exempt) {
    ExtraTemplate template = new ExtraTemplate();
    template.setName(name);
    template.setAmount(amount);
    template.setIsPercentage(isPercentage);
    template.setIsDefault(isDefault);
    template.setAddToSalary(addToSalary);
    template.setMultiplyYearly(multiplyYearly);
    template.setMultiplyMonthly(multiplyMonthly);
    template.setTaxConfigurationId(taxConfigurationId);
    template.setTaxIds(new HashSet<>(taxIds));
    template.setExempt(exempt || taxIds.isEmpty());
    return template;
  }

  private void seedParameters() {
    parameterRepository.saveAll(
        List.of(
            new Parameter(0, "iht", 20.0, null, null),
            new Parameter(0, "spp", 3.0, null, null),
            new Parameter(0, "scr", 0.9235, null, null)));
  }

  private void seedDemoData() {
    Faker faker = new Faker(Locale.ENGLISH, new Random(FAKER_SEED));
    List<ExtraTemplate> templates = extraTemplateRepository.findAll();

    List<Office> offices = new ArrayList<>();
    for (String[] office : OFFICES) {
      offices.add(officeRepository.save(new Office(0, office[0], office[1], new HashSet<>())));
    }

    List<Position> positions = new ArrayList<>();
    for (String[] position : POSITIONS) {
      positions.add(positionRepository.save(fakePosition(position)));
    }

    List<Candidate> candidates = new ArrayList<>();
    for (int i = 0; i < CANDIDATE_COUNT; i++) {
      Position position = positions.get(i * POSITION_COUNT / CANDIDATE_COUNT);
      candidates.add(candidateRepository.save(fakeCandidate(faker, position)));
    }

    LocalDateTime createdAt = FIRST_PROPOSAL_AT;
    int proposalIndex = 0;
    for (int i = 0; i < CANDIDATE_COUNT; i++) {
      if (!isFinalStage(i)) {
        continue;
      }
      Candidate candidate = candidates.get(i);
      Position position = candidate.getPosition();
      Office office = offices.get(faker.random().nextInt(offices.size()));
      createdAt = createdAt.plusDays(faker.random().nextInt(1, 5)).withHour(9 + proposalIndex % 8);
      Proposal proposal =
          proposalRepository.save(
              fakeProposal(faker, proposalIndex, createdAt, office, candidate, position));
      office.getProposals().add(proposal);
      candidate.getProposals().add(proposal);
      candidate.getNJoynSet().add(proposal.getNjoyn());
      candidate.setProposalsCount(candidate.getProposals().size());
      addStatuses(faker, proposal, createdAt);
      addExtras(faker, proposal.getSalary(), templates, proposal.getNjoyn());
      proposalIndex++;
    }
  }

  /**
   * Candidate {@code i} reaches the final stage (and gets a proposal) unless i is divisible by 3.
   */
  private static boolean isFinalStage(int candidateIndex) {
    return candidateIndex % 3 != 0;
  }

  private Position fakePosition(String[] position) {
    Position entity =
        new Position(0, position[0], position[1], position[2], position[3], new HashSet<>());
    stamp(entity, FIRST_POSITION_AT);
    return entity;
  }

  private Candidate fakeCandidate(Faker faker, Position position) {
    String name = faker.name().firstName();
    String surname = faker.name().lastName();
    String email =
        (name + "." + surname).toLowerCase(Locale.ROOT).replaceAll("[^a-z.]", "") + "@example.com";
    return new Candidate(
        0,
        name,
        surname,
        faker.numerify("#########"),
        faker.options().option("91", "92", "93", "96") + faker.numerify("#######"),
        email,
        "W-" + faker.numerify("####"),
        faker.random().nextInt(3),
        faker.options().option(0, 0, 1, 1, 2, 3),
        0,
        new HashSet<>(),
        new HashSet<>(),
        position);
  }

  private Proposal fakeProposal(
      Faker faker,
      int index,
      LocalDateTime createdAt,
      Office office,
      Candidate candidate,
      Position position) {
    String role = position.getTitle();
    int level = faker.random().nextInt(CONTRACT_LEVELS.length);
    boolean indefinite = faker.random().nextInt(10) < 7;
    LocalDate startDate = createdAt.toLocalDate().plusDays(faker.random().nextInt(14, 60));
    LocalDate dueDate =
        indefinite ? null : startDate.plusMonths(faker.options().option(6, 12, 24)).minusDays(1);

    Salary salary =
        new Salary(
            0,
            1000d + level * 350 + faker.random().nextInt(7) * 50,
            portugalConfiguration.getId(),
            null,
            new HashSet<>());
    Proposal proposal =
        new Proposal(
            0,
            position.getNjoyn() + "-" + (index + 1),
            position.getDescription()
                + " ("
                + candidate.getName()
                + " "
                + candidate.getSurname()
                + ")",
            role,
            indefinite,
            CONTRACT_LEVELS[level],
            startDate,
            dueDate,
            position.getKnowMore(),
            office,
            salary,
            candidate,
            position,
            new HashSet<>());
    salary.setProposal(proposal);
    stamp(proposal, createdAt);
    return proposal;
  }

  /** Adds a status history a few days apart; the latest status is the current one. */
  private void addStatuses(Faker faker, Proposal proposal, LocalDateTime createdAt) {
    LocalDateTime statusAt = createdAt;
    for (int code : STATUS_HISTORIES[faker.random().nextInt(STATUS_HISTORIES.length)]) {
      Status status = new Status(0, code, STATUS_MESSAGES[code], proposal);
      stamp(status, statusAt);
      proposal.getStatuses().add(statusRepository.save(status));
      statusAt =
          statusAt.plusDays(faker.random().nextInt(1, 6)).plusHours(faker.random().nextInt(8));
    }
  }

  /** Food allowance is always included; other benefits are picked at random. */
  private void addExtras(Faker faker, Salary salary, List<ExtraTemplate> templates, String njoyn) {
    Long configurationId = salary.getTaxConfigurationId();
    for (ExtraTemplate template : templates) {
      if (!configurationId.equals(template.getTaxConfigurationId())) {
        continue;
      }
      if (!"Food Allowance".equals(template.getName()) && faker.random().nextInt(100) >= 35) {
        continue;
      }
      Extra extra = new Extra(template);
      extra.setDescription(template.getName() + " included in proposal " + njoyn);
      extra.setIsDefault(template.getIsDefault());
      extra.setAddToSalary(template.getAddToSalary());
      extra.setExempt(template.getExempt());
      extra.setTaxIds(new HashSet<>(template.getTaxIds()));
      extra.setTemplateId(template.getId());
      extra.setSalary(salary);
      salary.getExtras().add(extraRepository.save(extra));
    }
  }

  private static void stamp(AuditableSuperclass entity, LocalDateTime at) {
    entity.setCreatedAt(at);
    entity.setModifiedAt(at);
  }
}
