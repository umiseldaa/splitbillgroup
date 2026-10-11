package com.project.allo.splitbill.service;

import com.project.allo.splitbill.dto.AddExpenseRequest;
import com.project.allo.splitbill.dto.CreateGroupRequest;
import com.project.allo.splitbill.model.Expense;
import com.project.allo.splitbill.model.Group;

public interface SplitBillService {
    
    Group createGroup(CreateGroupRequest req);
    Group addExpense(String groupId, AddExpenseRequest req);
    
}
