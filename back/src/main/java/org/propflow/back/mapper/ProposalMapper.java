package org.propflow.back.mapper;

import org.propflow.back.dto.ProposalDTO;
import java.util.stream.Collectors;
import org.propflow.back.dto.OfficeDTO;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Office;
import org.springframework.stereotype.Component;

/**
 * This class maps the Proposal model to the ProposalDTO
 */
@Component
public class ProposalMapper {

  public ProposalDTO toDTO(Proposal proposal) {
    ProposalDTO dto = new ProposalDTO();
    dto.setId(proposal.getId());
    dto.setDescription(proposal.getDescription());
    dto.setRole(proposal.getRole());
    dto.setDueDate(proposal.getDueDate());
    dto.setIndefinite(proposal.getIndefinite());
    dto.setContractLevel(proposal.getContractLevel());
    dto.setStartDate(proposal.getStartDate());
    dto.setStatuses(proposal.getStatuses());
    dto.setKnowMore(proposal.getKnowMore());
    dto.setSalary(proposal.getSalary());
    dto.setCandidate(proposal.getCandidate());
    dto.setNjoyn(proposal.getNjoyn());
    dto.setCreatedAt(proposal.getCreatedAt());
    dto.setCreatedBy(proposal.getCreatedBy());
    dto.setModifiedAt(proposal.getModifiedAt());
    dto.setModifiedBy(proposal.getModifiedBy());

    Office office = proposal.getOffice();
    OfficeDTO officeDTO = new OfficeDTO();
    officeDTO.setId(office.getId());
    officeDTO.setName(office.getName());
    officeDTO.setCode(office.getCode());
    officeDTO.setProposals(office.getProposals().stream().map(Proposal::getId).collect(Collectors.toSet()));

    dto.setOffice(officeDTO);

    return dto;
  }
}
