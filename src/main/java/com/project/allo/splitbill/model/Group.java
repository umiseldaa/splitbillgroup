package com.project.allo.splitbill.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Group {
    private String id;
    private String name;
    private List<String> participants;
    private List<Expense> expenses;
}
