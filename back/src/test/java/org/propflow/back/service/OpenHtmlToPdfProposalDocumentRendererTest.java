package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@ExtendWith(MockitoExtension.class)
class OpenHtmlToPdfProposalDocumentRendererTest {

  private OpenHtmlToPdfProposalDocumentRenderer renderer;

  @Mock private ProposalService proposalService;
  @Mock private SalaryService salaryService;

  @BeforeEach
  void setUp() {
    renderer = new OpenHtmlToPdfProposalDocumentRenderer();
    ReflectionTestUtils.setField(renderer, "proposalService", proposalService);
    ReflectionTestUtils.setField(renderer, "salaryService", salaryService);
    ReflectionTestUtils.setField(renderer, "templateEngine", templateEngine());
  }

  @ParameterizedTest
  @CsvSource({
    "en, Job Proposal, Role:, Candidate Details, Gross Salary",
    "es, Propuesta de Empleo, Puesto:, Detalles del Candidato, Salario Bruto",
    "pt, Proposta de Emprego, Função:, Detalhes do Candidato, Salário Bruto",
    "de, Stellenangebot, Rolle:, Details zum Kandidaten, Bruttogehalt"
  })
  void rendersLocalizedPdfText(
      String lang, String title, String role, String candidateDetails, String grossSalary)
      throws IOException {
    when(proposalService.one(1L)).thenReturn(proposal());
    when(salaryService.salaryCalculations(proposal(), null, null)).thenReturn(salaryBreakdown());

    byte[] pdf = renderer.render(1L, Locale.forLanguageTag(lang));

    try (PDDocument document = PDDocument.load(pdf)) {
      String text = new PDFTextStripper().getText(document);
      assertThat(text).contains(title, role, candidateDetails, grossSalary);
    }
  }

  @Test
  void longDescriptionIsRenderedInFullBesideItsLabel() throws IOException {
    String description =
        "Design and maintain the backend services that power proposal management,"
            + " collaborating with product and frontend teams";
    Proposal proposal = proposal();
    proposal.setDescription(description);
    when(proposalService.one(1L)).thenReturn(proposal);
    when(salaryService.salaryCalculations(proposal, null, null)).thenReturn(salaryBreakdown());

    byte[] pdf = renderer.render(1L, Locale.ENGLISH);

    try (PDDocument document = PDDocument.load(pdf)) {
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setSortByPosition(true);
      String text = stripper.getText(document);
      String descriptionLine =
          text.lines().filter(line -> line.contains("Description:")).findFirst().orElseThrow();
      assertThat(descriptionLine).contains("Design and");
      String unsortedText = new PDFTextStripper().getText(document).replaceAll("\\s+", " ");
      assertThat(unsortedText).contains(description);
    }
  }

  @Test
  void renderedPdfIsPdfA2uAndEmbedsFonts() throws IOException {
    when(proposalService.one(1L)).thenReturn(proposal());
    when(salaryService.salaryCalculations(proposal(), null, null)).thenReturn(salaryBreakdown());

    byte[] pdf = renderer.render(1L, Locale.ENGLISH);

    try (PDDocument document = PDDocument.load(pdf)) {
      PDDocumentCatalog catalog = document.getDocumentCatalog();
      assertThat(catalog.getOutputIntents()).isNotEmpty();

      PDMetadata metadata = catalog.getMetadata();
      assertThat(metadata).isNotNull();
      String xmp = metadata.getCOSObject().toTextString().toLowerCase(Locale.ROOT);
      assertThat(xmp)
          .contains(
              "pdf/a",
              "<pdfaid:part>2</pdfaid:part>",
              "<pdfaid:conformance>u</pdfaid:conformance>");

      Set<String> fontNames = new HashSet<>();
      for (PDPage page : document.getPages()) {
        PDResources resources = page.getResources();
        if (resources == null) {
          continue;
        }
        for (COSName name : resources.getFontNames()) {
          PDFont font = resources.getFont(name);
          if (font != null) {
            fontNames.add(font.getName());
          }
        }
      }
      assertThat(fontNames).isNotEmpty();
    }
  }

  private static Proposal proposal() {
    Office office = new Office();
    office.setName("Lisbon");
    office.setCode("LIS");

    Candidate candidate = new Candidate();
    candidate.setName("Ada");
    candidate.setSurname("Lovelace");
    candidate.setIdNumber("12345678");
    candidate.setPhone("+351 123 456 789");
    candidate.setEmail("ada@example.com");
    candidate.setWorkerId("W001");
    candidate.setMaritalStatus(0);
    candidate.setDependentsNumber(0);

    Salary salary = new Salary();
    salary.setBase(1000.0);

    Proposal proposal = new Proposal();
    proposal.setId(1L);
    proposal.setRole("Backend Engineer");
    proposal.setDescription("Senior backend position");
    proposal.setIndefinite(true);
    proposal.setContractLevel("Senior");
    proposal.setStartDate(LocalDate.of(2025, 1, 15));
    proposal.setOffice(office);
    proposal.setCandidate(candidate);
    proposal.setSalary(salary);
    return proposal;
  }

  private static Map<String, Map<String, Double>> salaryBreakdown() {
    Map<String, Double> monthly = new HashMap<>();
    monthly.put("Gross Salary", 1000.0);
    monthly.put("Base Salary", 900.0);
    monthly.put("IHT", 100.0);
    monthly.put("Share Purchase Plan", 30.0);
    monthly.put("Estimated Net Salary", 790.0);

    Map<String, Double> yearly = new HashMap<>();
    yearly.put("Gross Salary", 14000.0);
    yearly.put("Base Salary", 12600.0);
    yearly.put("IHT", 1400.0);
    yearly.put("Share Purchase Plan", 420.0);
    yearly.put("Estimated Net Salary", 11060.0);

    Map<String, Map<String, Double>> calculations = new HashMap<>();
    calculations.put("monthly", monthly);
    calculations.put("yearly", yearly);
    return calculations;
  }

  private static TemplateEngine templateEngine() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");

    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);
    engine.setTemplateEngineMessageSource(messageSource());
    return engine;
  }

  private static ReloadableResourceBundleMessageSource messageSource() {
    ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
    source.setBasename("classpath:messages");
    source.setDefaultEncoding("UTF-8");
    source.setFallbackToSystemLocale(false);
    return source;
  }
}
