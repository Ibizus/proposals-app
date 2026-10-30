package org.propflow.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** A single tax inside a {@link TaxConfiguration}. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "tax", indexes = @Index(name = "tax_config_index", columnList = "configuration_id"))
@Schema(description = "A tax within a configuration, either a fixed rate or progressive brackets.")
public class Tax extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotBlank
  @Schema(description = "Tax name, e.g. Social Security or IRPF.", example = "IRPF")
  private String name;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Schema(description = "FIXED rate or BRACKETED progressive brackets.", example = "BRACKETED")
  private TaxType type;

  @Schema(description = "Fixed rate in percent. Only used when type is FIXED.", example = "6.45")
  private Double fixedRate;

  @Schema(
      description =
          "When true, the tax only applies to extras that reference it, never to the base salary.",
      example = "false")
  private Boolean extrasOnly;

  @ManyToOne @NotNull private TaxConfiguration configuration;

  @OneToMany(mappedBy = "tax", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("threshold")
  @Schema(description = "Progressive brackets. Only used when type is BRACKETED.")
  private List<TaxBracket> brackets = new ArrayList<>();
}
