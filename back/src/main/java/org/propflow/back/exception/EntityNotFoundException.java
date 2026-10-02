package org.propflow.back.exception;

public class EntityNotFoundException extends RuntimeException{
    @SuppressWarnings("rawtypes")
    public EntityNotFoundException(Long id, Class entity){
        super("Not found Entity" + entity.getSimpleName() + " with id: " + id);
    }
}