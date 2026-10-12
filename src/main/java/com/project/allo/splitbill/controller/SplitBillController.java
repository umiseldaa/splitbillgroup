package com.project.allo.splitbill.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.project.allo.splitbill.dto.CreateGroupRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.allo.splitbill.dto.AddExpenseRequest;
import com.project.allo.splitbill.exception.BadRequestException;
import com.project.allo.splitbill.exception.NotFoundException;
import com.project.allo.splitbill.service.SplitBillService;



@Slf4j 
@RestController 
@RequestMapping("/api/splitbill/groups")
@RequiredArgsConstructor 
public class SplitBillController {

    @Autowired 
    private SplitBillService service;

    @GetMapping("")
    public String getMethodName() {
        log.info("masuk aja");
        return new String("OK");
    }
    

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody CreateGroupRequest req) {
        try {
            return success(HttpStatus.CREATED, service.createGroup(req));
        } catch (BadRequestException e) {
            log.warn("[Create Group] Failed: ", e);
            return failure(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("[Create Group] Error: ", e);
            return failure(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create group");
        }
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<Map<String, Object>> addExpense(@PathVariable String groupId,
        @RequestBody AddExpenseRequest req) {
        try {
            return success(HttpStatus.CREATED, service.addExpense(groupId, req));
        } catch (BadRequestException e) {
            log.warn("[Add Expense] Failed: ", e);
            return failure(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("[Add Expense] Error: ", e);
            return failure(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add expense");
        }
    }

    @GetMapping("/{groupId}/settlement")
    public ResponseEntity<Map<String, Object>> getSettlement(@PathVariable String groupId) {
        try {
            return success(HttpStatus.OK, service.getSettlement(groupId));
        } catch (NotFoundException e) {
            log.warn("[Get Settlement] Failed: ", e);
            return failure(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("[Get Settlement] Error: ", e);
            return failure(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get settlement");
        }
    }
    
    private ResponseEntity<Map<String, Object>> success(HttpStatus httpStatus, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", true);
        response.put("data", data);
        return new ResponseEntity<>(response, httpStatus);
    }

    private ResponseEntity<Map<String, Object>> failure(HttpStatus httpStatus, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", false);
        response.put("data", message);
        return new ResponseEntity<>(response, httpStatus);
    }


    
}
