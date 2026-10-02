package org.propflow.back.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaxesDTO {

    private double irs;
    private double ss;
}
