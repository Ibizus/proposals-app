package org.propflow.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A complete set of taxes for a country, used to calculate salary breakdowns.
 *
 * <p>Only one configuration can be active at a time. New proposals use the active configuration
 * unless they were created under a different one.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
    name = "tax_configuration",
    indexes = {
      @Index(name = "tax_config_country_index", columnList = "countryCode"),
      @Index(name = "tax_config_active_index", columnList = "active")
    })
@Schema(description = "A country-specific set of taxes and payment rules.")
public class TaxConfiguration extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotBlank
  @Schema(description = "ISO country code the configuration represents.", example = "es")
  private String countryCode;

  @NotBlank
  @Schema(
      description = "Human-readable label, including that the rates are illustrative.",
      example = "Spain (illustrative example data)")
  private String label;

  @NotNull
  @Positive
  @Schema(description = "Number of salary payments per year.", example = "12")
  private Integer yearlyPayments;

  @NotNull
  @Schema(description = "Whether this is the configuration currently used for new proposals.")
  private Boolean active;

  @OneToMany(mappedBy = "configuration", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id")
  @Schema(description = "Taxes that apply to salaries under this configuration.")
  private List<Tax> taxes = new ArrayList<>();
}
