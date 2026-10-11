package com.project.allo.splitbill.repository;

import java.util.Optional;

import com.project.allo.splitbill.model.Group;

public interface SplitBillRepository {
    
    Group save(Group group);

    Optional<Group> findById(String id);
}
