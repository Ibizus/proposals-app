package org.propflow.back.repository;

import java.util.List;

import org.propflow.back.model.ExtraTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraTemplateRepository extends JpaRepository<ExtraTemplate, Long>{

    public List<ExtraTemplate> findExtraTemplatesByNameContainingIgnoreCase(String name);
}
