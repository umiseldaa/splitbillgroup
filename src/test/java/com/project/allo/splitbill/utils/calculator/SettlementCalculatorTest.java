package com.project.allo.splitbill.utils.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.project.allo.splitbill.model.Expense;
import com.project.allo.splitbill.model.settlement.Transfer;

class SettlementCalculatorTest {

    private final SettlementCalculator calculator = new SettlementCalculator();

    @Test
    void onePersonPaysForEveryone_othersOweThem() {
        // A paid 300.00 for A, B and C (100 each)
        Expense lunch = expense("A", "300.00", "A", "B", "C");

        Map<String, Object> result = calculator.settle(List.of("A", "B", "C"), List.of(lunch));
        Map<String, BigDecimal> balances = getBalances(result);
        List<Transfer> transfers = getTransfers(result);

        assertEquals(new BigDecimal("200.00"), balances.get("A")); // A should get 200 back
        assertEquals(new BigDecimal("-100.00"), balances.get("B"));
        assertEquals(new BigDecimal("-100.00"), balances.get("C"));
        assertEquals(2, transfers.size());
        assertTrue(transfers.contains(new Transfer("B", "A", new BigDecimal("100.00"))));
        assertTrue(transfers.contains(new Transfer("C", "A", new BigDecimal("100.00"))));
    }

    @Test
    void debtsThatCancelEachOtherProduceNoTransfer() {
        // A paid 100 for A+B (B owes 50), then B paid 100 for A+B (A owes 50). They are even.
        Expense first = expense("A", "100.00", "A", "B");
        Expense second = expense("B", "100.00", "A", "B");

        Map<String, Object> result = calculator.settle(List.of("A", "B"), List.of(first, second));

        assertTrue(getTransfers(result).isEmpty());
    }

    @Test
    void chainOfDebtsIsShortenedToOneTransfer() {
        // B paid 10 for A (A owes B 10), C paid 10 for B (B owes C 10).
        // B is just a middleman, so A should pay C directly.
        Expense e1 = expense("B", "10.00", "A");
        Expense e2 = expense("C", "10.00", "B");

        Map<String, Object> result = calculator.settle(List.of("A", "B", "C"), List.of(e1, e2));

        assertEquals(List.of(new Transfer("A", "C", new BigDecimal("10.00"))), getTransfers(result));
    }

    @Test
    void leftoverCentIsAbsorbedByThePayer() {
        // A paid 100.00 for A, B, C. Share = 33.33, so 1 cent is left over.
        // B and C owe 33.33 each, A bears the leftover cent (A only gets 66.66 back).
        Expense dinner = expense("A", "100.00", "A", "B", "C");

        Map<String, Object> result = calculator.settle(List.of("A", "B", "C"), List.of(dinner));
        Map<String, BigDecimal> balances = getBalances(result);

        assertEquals(new BigDecimal("66.66"), balances.get("A"));
        assertEquals(new BigDecimal("-33.33"), balances.get("B"));
        assertEquals(new BigDecimal("-33.33"), balances.get("C"));
    }

    @Test
    void payerWhoIsNotAParticipantStillAbsorbsTheLeftoverCent() {
        // A paid 100.00 for B, C, D only (A is not in the list). Share = 33.33 each.
        Expense dinner = expense("A", "100.00", "B", "C", "D");

        Map<String, Object> result = calculator.settle(List.of("A", "B", "C", "D"), List.of(dinner));
        Map<String, BigDecimal> balances = getBalances(result);

        assertEquals(new BigDecimal("99.99"), balances.get("A"));
        assertEquals(new BigDecimal("-33.33"), balances.get("B"));
        assertEquals(new BigDecimal("-33.33"), balances.get("C"));
        assertEquals(new BigDecimal("-33.33"), balances.get("D"));
    }

    @Test
    void shareIsRoundedDown_notHalfUp() {
        // A paid 0.05 for B and C. 0.05 / 2 = 0.025, rounded down = 0.02 (normal rounding would give 0.03).
        Expense snack = expense("A", "0.05", "B", "C");

        Map<String, Object> result = calculator.settle(List.of("A", "B", "C"), List.of(snack));
        Map<String, BigDecimal> balances = getBalances(result);

        assertEquals(new BigDecimal("-0.02"), balances.get("B"));
        assertEquals(new BigDecimal("-0.02"), balances.get("C"));
        assertEquals(new BigDecimal("0.04"), balances.get("A")); // A bears the missing cent
    }

    @Test
    void payerPayingOnlyForThemselvesChangesNothing() {
        Expense alone = expense("A", "50.00", "A");

        Map<String, Object> result = calculator.settle(List.of("A", "B"), List.of(alone));

        assertEquals(new BigDecimal("0.00"), getBalances(result).get("A"));
        assertTrue(getTransfers(result).isEmpty());
    }

    @Test
    void balancesSumToZero_andTransfersSettleEveryone() {
        Expense e1 = expense("A", "100.00", "A", "B", "C"); // has a leftover cent
        Expense e2 = expense("B", "45.50", "B", "C");
        Expense e3 = expense("C", "80.00", "A", "B", "C");

        Map<String, Object> result = calculator.settle(List.of("A", "B", "C"), List.of(e1, e2, e3));
        Map<String, BigDecimal> balances = getBalances(result);

        // 1) nobody gains or loses money overall
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal balance : balances.values()) {
            total = total.add(balance);
        }
        assertEquals(0, total.compareTo(BigDecimal.ZERO));

        // 2) actually "play" the transfers and check everyone ends at zero
        Map<String, BigDecimal> after = new LinkedHashMap<>(balances);
        for (Transfer t : getTransfers(result)) {
            after.put(t.getFrom(), after.get(t.getFrom()).add(t.getAmount()));
            after.put(t.getTo(), after.get(t.getTo()).subtract(t.getAmount()));
        }
        for (BigDecimal left : after.values()) {
            assertEquals(0, left.compareTo(BigDecimal.ZERO));
        }
    }

    // ---- small helpers so each test reads like a story ----

    // expense("A", "300.00", "A", "B") = A paid 300 for A and B
    private Expense expense(String paidBy, String amount, String... forWho) {
        return new Expense("id", "test", paidBy, new BigDecimal(amount), List.of(forWho));
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> getBalances(Map<String, Object> result) {
        return (Map<String, BigDecimal>) result.get("balances");
    }

    @SuppressWarnings("unchecked")
    private List<Transfer> getTransfers(Map<String, Object> result) {
        return (List<Transfer>) result.get("transfers");
    }

}
