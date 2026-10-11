package com.project.allo.splitbill.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.allo.splitbill.dto.AddExpenseRequest;
import com.project.allo.splitbill.dto.CreateGroupRequest;
import com.project.allo.splitbill.exception.BadRequestException;
import com.project.allo.splitbill.exception.NotFoundException;
import com.project.allo.splitbill.model.Expense;
import com.project.allo.splitbill.model.Group;
import com.project.allo.splitbill.repository.SplitBillRepository;
import com.project.allo.splitbill.service.SplitBillService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SplitBillServiceImpl implements SplitBillService {

    @Autowired 
    private SplitBillRepository repo;

    @Override
    public Group createGroup(CreateGroupRequest req) {

        if (req == null || req.getName() == null || req.getName().isBlank()) {
            throw new BadRequestException("name is required");
        }

        if (req.getParticipants() == null || req.getParticipants().size() < 2) {
            throw new BadRequestException("a group needs at least 2 participants");
        }

        List<String> participants = new ArrayList<>();
        for (String name : req.getParticipants()) {
            if (name == null || name.isBlank()) {
                throw new BadRequestException("particapnt names must not be blank");
            }
            participants.add(name.trim());
        }

        Set<String> uniqueParticipants = new HashSet<>(participants);
        if (uniqueParticipants.size() < participants.size()) {
            throw new BadRequestException("participant names must be unique");
        }

        Group group = new Group(UUID.randomUUID().toString(), req.getName().trim(), participants, new ArrayList<>());
        return repo.save(group);
    }

    @Override
    public Expense addExpense(String groupId, AddExpenseRequest req) {
        Group group = findGroup(groupId);

        if (req == null || req.getDescription() == null || req.getDescription().isBlank()) {
            throw new BadRequestException("description is required");
        }
        if (req.getPaidBy() == null || !group.getParticipants().contains(req.getPaidBy().trim())) {
            throw new BadRequestException("paid_by must be a participant of this group");
        }
        
        BigDecimal amount = req.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("amount must be greater than 0");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException("amount must have at most 2 decimal places");
        }
        
        List<String> participants = req.getParticipants();
        if (participants == null || participants.isEmpty()) {
            throw new BadRequestException("participants is required");
        }

        Set<String> uniqueParticipants = new HashSet<>(participants);
        if (uniqueParticipants.size() < participants.size()) {
            throw new BadRequestException("participants must not contain duplicates");
        }
        for(String name: participants) {
            if(!group.getParticipants().contains(name)) {
                throw new BadRequestException("'"+name+"' is not a participant of this group");
            }
        }

        Expense expense = new Expense(UUID.randomUUID().toString(), req.getDescription().trim(), req.getPaidBy().trim(),
                amount.setScale(2, RoundingMode.UNNECESSARY), participants);
        
        group.getExpenses().add(expense);
        repo.save(group);

        return expense;

    }

    private Group findGroup(String groupId) {
        return repo.findById(groupId).orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
    }

}
