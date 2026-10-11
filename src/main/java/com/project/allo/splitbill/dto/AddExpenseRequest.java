package com.project.allo.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data 
public class AddExpenseRequest {
    private String description;
    private String paidBy;
    private BigDecimal amount;
    private List<String> participants;
}
