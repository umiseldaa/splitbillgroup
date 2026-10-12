package com.project.allo.splitbill.model.settlement;

import java.math.BigDecimal;

import lombok.Data;

public record Transfer(
    String from,
    String to,
    BigDecimal amount
){}
