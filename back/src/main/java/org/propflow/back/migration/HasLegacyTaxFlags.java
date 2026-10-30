package org.propflow.back.migration;

import java.util.Set;

/**
 * Common legacy tax fields shared by {@link org.propflow.back.model.Extra} and {@link
 * org.propflow.back.model.ExtraTemplate}.
 */
public interface HasLegacyTaxFlags {

  Boolean getIsSSTaxable();

  Boolean getIsIRSTaxable();

  Set<Long> getTaxIds();

  void setTaxIds(Set<Long> taxIds);

  void setExempt(Boolean exempt);
}
