package org.propflow.back.dto;

import java.util.Set;

/**
 * This DTO was created to solve a problem where fetching a proposal would have an Office object
 * with a set of Proposals and it can result in an infinite loop. So ProposalDTO has an OfficeDTO
 * property instead of an Office, and the OfficeDTO has a set of the ids of the proposals it has, so
 * this should solve the infinite loop problem.
 */
public record OfficeDTO(long id, String name, String code, Set<Long> proposals) {}
