package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.Mapper;
import com.buildscheduler.buildscheduler.service.base.BaseService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicServiceImpl<E, D, ID> implements BaseService<D, ID> {
    protected final JpaRepository<E, ID> repository;
    protected final Mapper<E, D> mapper;

    public DynamicServiceImpl(JpaRepository<E, ID> repository, Mapper<E, D> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override public D create(D dto) {
        E entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override public List<D> getAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override public D getById(ID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
    }

    @Override public D update(ID id, D dto) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Entity not found");
        }
        E entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Entity not found");
        }
        repository.deleteById(id);
    }
}