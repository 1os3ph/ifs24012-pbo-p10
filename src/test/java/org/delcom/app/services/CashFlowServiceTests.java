package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.CashFlow;
import org.delcom.app.repositories.CashFlowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CashFlowServiceTests {

    @Mock
    private CashFlowRepository cashFlowRepository;

    @InjectMocks
    private CashFlowService cashFlowService;

    @Test
    @DisplayName("Pengujian lengkap untuk CashFlowService")
    void testCashFlowService() {
        // 1. Persiapan Data
        UUID userId = UUID.randomUUID();
        UUID cashFlowId = UUID.randomUUID();
        UUID nonexistentId = UUID.randomUUID();

        // Constructor P10: (userId, type, source, label, amount, description)
        CashFlow cashFlow = new CashFlow(userId, "IN", "BANK", "Salary", 1000, "Monthly Salary");
        cashFlow.setId(cashFlowId);

        // ==========================================
        // TEST 1: createCashFlow
        // ==========================================
        {
            when(cashFlowRepository.save(any(CashFlow.class))).thenReturn(cashFlow);

            CashFlow created = cashFlowService.createCashFlow(userId, "IN", "BANK", "Salary", 1000, "Monthly Salary");

            assertNotNull(created);
            assertEquals(userId, created.getUserId());
            assertEquals("IN", created.getType());
        }

        // ==========================================
        // TEST 2: getAllCashFlows (Tanpa Search / NULL)
        // ==========================================
        {
            // P10 menggunakan findByUserId, bukan findAll
            when(cashFlowRepository.findByUserId(userId)).thenReturn(List.of(cashFlow));

            List<CashFlow> result = cashFlowService.getAllCashFlows(userId, null);

            assertEquals(1, result.size());
            assertEquals(cashFlowId, result.get(0).getId());
        }

        // ==========================================
        // TEST 3: getAllCashFlows (Dengan Search Valid)
        // ==========================================
        {
            String keyword = "BANK";
            when(cashFlowRepository.findByKeyword(eq(userId), eq(keyword))).thenReturn(List.of(cashFlow));

            List<CashFlow> result = cashFlowService.getAllCashFlows(userId, keyword);

            assertEquals(1, result.size());
            assertEquals("BANK", result.get(0).getSource());
        }

        // ==========================================
        // TEST 3.5: getAllCashFlows (Search Kosong/Spasi) - COVERAGE FIX
        // ==========================================
        {
            String emptyKeyword = "   ";
            // Jika kosong, harusnya panggil findByUserId lagi
            when(cashFlowRepository.findByUserId(userId)).thenReturn(List.of(cashFlow));

            List<CashFlow> result = cashFlowService.getAllCashFlows(userId, emptyKeyword);

            assertEquals(1, result.size());
            // Pastikan findByUserId dipanggil 2 kali (sekali saat null, sekali saat empty string)
            verify(cashFlowRepository, times(2)).findByUserId(userId);
        }

        // ==========================================
        // TEST 4: getCashFlowById (Sukses & Gagal)
        // ==========================================
        {
            when(cashFlowRepository.findByUserIdAndId(userId, cashFlowId)).thenReturn(Optional.of(cashFlow));
            when(cashFlowRepository.findByUserIdAndId(userId, nonexistentId)).thenReturn(Optional.empty());

            // Case Ada
            CashFlow found = cashFlowService.getCashFlowById(userId, cashFlowId);
            assertNotNull(found);
            assertEquals(cashFlowId, found.getId());

            // Case Tidak Ada
            CashFlow notFound = cashFlowService.getCashFlowById(userId, nonexistentId);
            assertNull(notFound);
        }

        // ==========================================
        // TEST 5: getCashFlowLabels
        // ==========================================
        {
            // P10 menggunakan findDistinctLabelsByUserId
            when(cashFlowRepository.findDistinctLabelsByUserId(userId)).thenReturn(List.of("Salary"));

            List<String> labels = cashFlowService.getCashFlowLabels(userId);

            assertEquals(1, labels.size());
            assertEquals("Salary", labels.get(0));
        }

        // ==========================================
        // TEST 6: updateCashFlow (Sukses)
        // ==========================================
        {
            when(cashFlowRepository.findByUserIdAndId(userId, cashFlowId)).thenReturn(Optional.of(cashFlow));
            when(cashFlowRepository.save(any(CashFlow.class))).thenAnswer(i -> i.getArgument(0));

            CashFlow updated = cashFlowService.updateCashFlow(
                userId, cashFlowId, "OUT", "ATM", "Withdraw", 500, "Desc"
            );

            assertNotNull(updated);
            assertEquals("OUT", updated.getType());
            assertEquals("ATM", updated.getSource());
        }

        // ==========================================
        // TEST 7: updateCashFlow (Gagal / Not Found)
        // ==========================================
        {
            when(cashFlowRepository.findByUserIdAndId(userId, nonexistentId)).thenReturn(Optional.empty());

            CashFlow result = cashFlowService.updateCashFlow(
                userId, nonexistentId, "OUT", "ATM", "Withdraw", 500, "Desc"
            );

            assertNull(result);
        }

        // ==========================================
        // TEST 8: deleteCashFlow (Sukses & Gagal)
        // ==========================================
        {
            // Sukses
            when(cashFlowRepository.findByUserIdAndId(userId, cashFlowId)).thenReturn(Optional.of(cashFlow));
            assertTrue(cashFlowService.deleteCashFlow(userId, cashFlowId));
            verify(cashFlowRepository, times(1)).deleteById(cashFlowId);

            // Gagal
            when(cashFlowRepository.findByUserIdAndId(userId, nonexistentId)).thenReturn(Optional.empty());
            assertFalse(cashFlowService.deleteCashFlow(userId, nonexistentId));
        }
    }
}