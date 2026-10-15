package org.propflow.back.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.propflow.back.dto.ProposalDTO;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;

class ProposalMapperTest {

  private final ProposalMapper mapper = new ProposalMapper();

  @Test
  void mapsProposalFieldsAndFlattensOfficeProposalsToIds() {
    Proposal proposal = new Proposal();
    proposal.setId(10);
    proposal.setNjoyn("NJ-10");
    proposal.setDescription("Backend developer");
    proposal.setRole("Developer");
    proposal.setStartDate(LocalDate.of(2024, 5, 1));
    proposal.setDueDate(LocalDate.of(2024, 4, 15));
    proposal.setIndefinite(true);
    proposal.setContractLevel("B2");
    proposal.setKnowMore("More info");
    Proposal sibling = new Proposal();
    sibling.setId(11);
    Office office = new Office(3, "Lisbon", "LIS", new HashSet<>(Set.of(proposal, sibling)));
    Salary salary = new Salary(4, 1500d, proposal, new HashSet<>());
    Candidate candidate = new Candidate();
    candidate.setName("Ann");
    proposal.setOffice(office);
    proposal.setSalary(salary);
    proposal.setCandidate(candidate);

    ProposalDTO dto = mapper.toDTO(proposal);

    assertThat(dto.getId()).isEqualTo(10);
    assertThat(dto.getNjoyn()).isEqualTo("NJ-10");
    assertThat(dto.getDescription()).isEqualTo("Backend developer");
    assertThat(dto.getRole()).isEqualTo("Developer");
    assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2024, 5, 1));
    assertThat(dto.getDueDate()).isEqualTo(LocalDate.of(2024, 4, 15));
    assertThat(dto.getIndefinite()).isTrue();
    assertThat(dto.getContractLevel()).isEqualTo("B2");
    assertThat(dto.getKnowMore()).isEqualTo("More info");
    assertThat(dto.getSalary()).isSameAs(salary);
    assertThat(dto.getCandidate()).isSameAs(candidate);
    assertThat(dto.getCreatedAt()).isEqualTo(proposal.getCreatedAt());
    assertThat(dto.getOffice().getId()).isEqualTo(3);
    assertThat(dto.getOffice().getName()).isEqualTo("Lisbon");
    assertThat(dto.getOffice().getCode()).isEqualTo("LIS");
    assertThat(dto.getOffice().getProposals()).containsExactlyInAnyOrder(10L, 11L);
  }
}
