package com.project.allo.splitbill.utils.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.project.allo.splitbill.model.Expense;
import com.project.allo.splitbill.model.settlement.Transfer;

@Component 
public class SettlementCalculator {
    
    public Map<String, Object> settle(List<String> participants, List<Expense> expenses) {
        // saldo everyone start from 0.00
        Map<String, BigDecimal> balances = new LinkedHashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            balances.put(participants.get(i), BigDecimal.ZERO.setScale(2));
        }

        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            String paidBy = expense.getPaidBy();
            List<String> expenseParticipants = expense.getParticipants();

            // jatah tiap orang, dibulatkan ke bawah
            BigDecimal share = calcShare(expense.getAmount(), expenseParticipants.size());

            for (int j = 0; j < expenseParticipants.size(); j++) {
                String name = expenseParticipants.get(j);

                // si pembayar nggak utang ke dirinya sendiri
                if (name.equals(paidBy)) {
                    continue;
                }

                // participant: utang bertambah, pembayar: harus dibayar balik bertambah
                balances.put(name, balances.get(name).subtract(share));
                balances.put(paidBy, balances.get(paidBy).add(share));
            }
        }
        
        // pisahin: yang masih utang (debtors) dan yang harus dibayar balik (creditors)
        // dua-duanya nilainya dibikin positif biar gampang dihitung
        Map<String, BigDecimal> debtors = new LinkedHashMap<>();
        Map<String, BigDecimal> creditors = new LinkedHashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            String name = participants.get(i);
            BigDecimal balance = balances.get(name);
            if (balance.signum() < 0) {
                debtors.put(name, balance.negate());
            } else if (balance.signum() > 0) {
                creditors.put(name, balance);
            }
        }

        // pasangin: tiap yang utang bayar ke yang harus dibayar balik sampai utangnya lunas
        List<String> debtorNames = new ArrayList<>(debtors.keySet());
        List<String> creditorNames = new ArrayList<>(creditors.keySet());
        List<Transfer> transfers = new ArrayList<>();

        for (int i = 0; i < debtorNames.size(); i++) {
            String debtorName = debtorNames.get(i);

            for (int j = 0; j < creditorNames.size(); j++) {
                String creditorName = creditorNames.get(j);
                BigDecimal debtLeft = debtors.get(debtorName);
                BigDecimal owedLeft = creditors.get(creditorName);

                // utangnya sudah lunas, lanjut ke orang berikutnya
                if (debtLeft.signum() == 0) {
                    break;
                }
                // dia sudah dibayar penuh, cari yang lain
                if (owedLeft.signum() == 0) {
                    continue;
                }

                // bayar sebanyak mungkin: yang lebih kecil antara sisa utang dan sisa piutang
                BigDecimal pay = debtLeft.min(owedLeft);
                transfers.add(new Transfer(debtorName, creditorName, pay));

                debtors.put(debtorName, debtLeft.subtract(pay));
                creditors.put(creditorName, owedLeft.subtract(pay));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("balances", balances);
        result.put("transfers", transfers);
        return result;
    }

    // 100.00/3=33.333...., jatah = 33.33 -> sisa 1 ditanggung pembayar
    private BigDecimal calcShare(BigDecimal amount, int totalParticipants) {
        return amount.divide(BigDecimal.valueOf(totalParticipants), 2, RoundingMode.DOWN);
    }

}
