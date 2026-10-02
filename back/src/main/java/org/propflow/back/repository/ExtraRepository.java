package org.propflow.back.repository;

import java.util.List;

import org.propflow.back.model.Extra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraRepository extends JpaRepository <Extra, Long>{

    public List<Extra> findExtrasByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
