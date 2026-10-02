package org.propflow.back.serializer;


import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.util.Set;
import java.util.stream.Collectors;

import org.propflow.back.model.Proposal;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This serializer is made to mapp a set of Proposal to a set of id's
 * It is used on the Candidate model to return the id's of the proposals
 * instead of all the Proposal objects
 */
public class ProposalSerializer extends StdSerializer<Set<Proposal>> {

  public ProposalSerializer() {
    this(null);
  }

  public ProposalSerializer(Class<Set<Proposal>> t) {
    super(t);
  }

  @Override
  public void serialize(
      Set<Proposal> proposals,
      JsonGenerator generator,
      SerializerProvider provider)
      throws IOException, JsonProcessingException {

    Set<Long> ids = proposals.stream()
        .map(Proposal::getId)
        .collect(Collectors.toSet());
    generator.writeObject(ids);
  }
}
