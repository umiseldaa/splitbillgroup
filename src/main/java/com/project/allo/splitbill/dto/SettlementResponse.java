package com.project.allo.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.project.allo.splitbill.model.settlement.Transfer;

import lombok.Data;

@Data 
public class SettlementResponse {
    private String groupId;
    private String groupName;
    private BigDecimal totalExpenses;
    private int serviceChargePct;
    private BigDecimal serviceChargeAmount;
    private Map<String, BigDecimal> balances;
    private List<Transfer> settlements;
}
