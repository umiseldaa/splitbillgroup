package com.project.allo.splitbill.dto;

import java.util.List;

import lombok.Data;

@Data 
public class CreateGroupRequest {
    private String name;
    private List<String> participants;
}
