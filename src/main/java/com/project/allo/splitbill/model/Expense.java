package com.project.allo.splitbill.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class Expense {
    private String id;
    private String description;
    private String paidBy;
    private BigDecimal amount;
    private List<String> participants;
}
