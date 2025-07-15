package com.buildscheduler.buildscheduler.service.base;

import java.util.List;

public interface BaseService<D, ID> {
    D create(D dto);
    List<D> getAll();
    D getById(ID id);
    D update(ID id, D dto);
    void delete(ID id);
}
