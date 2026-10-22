package org.propflow.back.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.propflow.back.dto.OfficeDTO;
import org.propflow.back.dto.ProposalDTO;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.springframework.stereotype.Component;

/** This class maps the Proposal model to the ProposalDTO. */
@Component
public class ProposalMapper {

  public ProposalDTO toDTO(Proposal proposal) {
    Office office = proposal.getOffice();
    Set<Long> proposalIds =
        office.getProposals().stream().map(Proposal::getId).collect(Collectors.toSet());
    OfficeDTO officeDTO =
        new OfficeDTO(office.getId(), office.getName(), office.getCode(), proposalIds);

    return new ProposalDTO(
        proposal.getId(),
        proposal.getDescription(),
        proposal.getRole(),
        proposal.getContractLevel(),
        proposal.getIndefinite(),
        proposal.getStartDate(),
        proposal.getDueDate(),
        null,
        proposal.getKnowMore(),
        officeDTO,
        proposal.getSalary(),
        proposal.getCandidate(),
        proposal.getStatuses(),
        proposal.getNjoyn(),
        proposal.getCreatedAt(),
        proposal.getCreatedBy(),
        proposal.getModifiedAt(),
        proposal.getModifiedBy());
  }
}
