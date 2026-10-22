package org.propflow.back.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.propflow.back.dto.ProposalDTO;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Position;
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
    Position position = new Position();
    position.setId(5);
    proposal.setPosition(position);

    ProposalDTO dto = mapper.toDTO(proposal);

    assertThat(dto.id()).isEqualTo(10);
    assertThat(dto.njoyn()).isEqualTo("NJ-10");
    assertThat(dto.description()).isEqualTo("Backend developer");
    assertThat(dto.role()).isEqualTo("Developer");
    assertThat(dto.startDate()).isEqualTo(LocalDate.of(2024, 5, 1));
    assertThat(dto.dueDate()).isEqualTo(LocalDate.of(2024, 4, 15));
    assertThat(dto.indefinite()).isTrue();
    assertThat(dto.contractLevel()).isEqualTo("B2");
    assertThat(dto.knowMore()).isEqualTo("More info");
    assertThat(dto.salary()).isSameAs(salary);
    assertThat(dto.candidate()).isSameAs(candidate);
    assertThat(dto.position()).isSameAs(position);
    assertThat(dto.createdAt()).isEqualTo(proposal.getCreatedAt());
    assertThat(dto.office().id()).isEqualTo(3);
    assertThat(dto.office().name()).isEqualTo("Lisbon");
    assertThat(dto.office().code()).isEqualTo("LIS");
    assertThat(dto.office().proposals()).containsExactlyInAnyOrder(10L, 11L);
  }
}
