package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class MailTemplateI18nTest {

  private SpringTemplateEngine engine;

  @BeforeEach
  void setUp() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");

    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(false);

    engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);
    engine.setTemplateEngineMessageSource(messageSource);
  }

  @ParameterizedTest(name = "offer template in {0}")
  @CsvSource({
    "en, indefinite, Hello Ada|Contract Type: indefinite contract|Monthly Remuneration: 1000.0"
        + " gross monthly",
    "pt, sem termo, Olá Ada|Tipo de Contrato: Contrato sem termo|Remuneração Mensal: 1000.0€"
        + " ilíquidos mensais",
    "de, Unbefristeter, Hallo Ada|Vertragstyp: Unbefristeter Vertrag|Monatliche Vergütung: 1000.0"
        + " brutto pro Monat",
    "es, indefinido, Hola Ada|Tipo de Contrato: contrato indefinido|Remuneración Mensual: 1000.0"
        + " bruto mensual",
  })
  void offerTemplateRendersInEachLanguage(String lang, String contract, String expected) {
    String html = render("proposal.html", lang, offerModel(contract));

    for (String phrase : expected.split("\\|")) {
      assertThat(html).contains(phrase);
    }
  }

  @ParameterizedTest(name = "candidate offer template in {0}")
  @CsvSource({
    "en, Dear Ada|I am pleased to extend a formal offer",
    "pt, Caro Ada|É com satisfação que apresento uma proposta formal",
    "de, Sehr geehrte(r) Ada|Es freut mich",
    "es, Estimado/a Ada|Me complace presentarle una oferta formal",
  })
  void candidateOfferTemplateRendersInEachLanguage(String lang, String expected) {
    String html = render("candidateOffer.html", lang, candidateModel());

    for (String phrase : expected.split("\\|")) {
      assertThat(html).contains(phrase);
    }
  }

  @ParameterizedTest(name = "unknown language {0} falls back to English")
  @ValueSource(strings = {"fr", "zz"})
  void unknownLanguageFallsBackToEnglish(String lang) {
    assertThat(render("proposal.html", lang, offerModel("indefinite")))
        .contains("Hello Ada", "Contract Type: indefinite contract");
    assertThat(render("candidateOffer.html", lang, candidateModel())).contains("Dear Ada");
  }

  @Test
  void portugueseOfferIntroNamesTheEmployer() {
    assertThat(render("proposal.html", "pt", offerModel("sem termo")))
        .contains(
            "Na sequência da conversa efetuada, apresentamos de seguida as condições propostas"
                + " para a tua admissão na PropFlowTI Portugal, SA:");
  }

  @Test
  void userEnteredNamesAreHtmlEscaped() {
    Map<String, Object> offer = offerModel("indefinite");
    offer.put("office", "<a href=\"https://evil.example\">x</a>");
    Map<String, Object> candidate = candidateModel();
    candidate.put("role", "<b>Engineer</b>");
    candidate.put("office", "<a href=\"https://evil.example\">x</a>");

    assertThat(render("proposal.html", "en", offer))
        .doesNotContain("<a href")
        .contains("&lt;a href=");
    assertThat(render("candidateOffer.html", "en", candidate))
        .doesNotContain("<a href", "<b>Engineer</b>")
        .contains("&lt;b&gt;Engineer&lt;/b&gt;", "<br/>");
  }

  private String render(String template, String lang, Map<String, Object> model) {
    Context context = new Context(Locale.forLanguageTag(lang));
    context.setVariables(model);
    return engine.process(template, context);
  }

  private static Map<String, Object> offerModel(String contract) {
    Map<String, Object> model = new HashMap<>();
    model.put("candidate", "Ada");
    model.put("office", "Lisbon Oriente");
    model.put("contract", contract);
    model.put("date", "2025-01-15");
    model.put("monthlyGross", "1000.0");
    model.put("yearlyGross", "14000.0");
    model.put("base", "900.0");
    model.put("iht", "100.0");
    model.put("food", "5.5");
    model.put("carMonthly", "200.0");
    model.put("carYearly", "2800.0");
    model.put("fuelYearly", "600.0");
    model.put("fuelMonthly", "50.0");
    model.put("yearlyFlex", "10.0");
    return model;
  }

  private static Map<String, Object> candidateModel() {
    Map<String, Object> model = new HashMap<>();
    model.put("candidate", "Ada");
    model.put("role", "Backend Engineer");
    model.put("office", "Lisbon");
    return model;
  }
}
