package org.propflow.back.service;

import java.util.List;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Extra;
import org.propflow.back.repository.ExtraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtraService {

    @Autowired
    private ExtraRepository extraRepository;

    public List<Extra> all(){return this.extraRepository.findAll();}

    public List<Extra> findByNameAndDescription(String input){
        return extraRepository.findExtrasByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(input, input);
    }

    public Extra save(Extra extra){return this.extraRepository.save(extra);}

    public Extra one(Long id){
        return this.extraRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException(id, Extra.class));
    }

    public Extra replace(Long id, Extra extra){
        return this.extraRepository.findById(id).map( b -> {
            if (id.equals(extra.getId())) return this.extraRepository.save(extra);
            else throw new NotCouplingIdException(id, extra.getId(), Extra.class);
        }).orElseThrow(()-> new EntityNotFoundException(id, Extra.class));
    }

    public void delete(Long id){
        this.extraRepository.findById(id).map( b -> {this.extraRepository.delete(b);
        return b;})
                .orElseThrow(()-> new EntityNotFoundException(id, Extra.class));
    }
}
