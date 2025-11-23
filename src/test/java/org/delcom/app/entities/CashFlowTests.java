package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CashFlowTests {

    @Test
    @DisplayName("Membuat instance dari kelas CashFlow")
    void testMembuatInstanceCashFlow() throws Exception {
        
        // Skenario 1: CashFlow dengan Constructor Lengkap
        {
            UUID userId = UUID.randomUUID();
            CashFlow cashFlow = new CashFlow(userId, "Inflow", "Gaji", "gaji-bulanan", 400000,
                    "Menerima gaji bulanan dari perusahaan.");

            // Pengecekan menggunakan JUnit Assertions
            assertEquals(userId, cashFlow.getUserId()); // Cek apakah userId tersimpan
            assertEquals("Inflow", cashFlow.getType());
            assertEquals("Gaji", cashFlow.getSource());
            assertEquals("gaji-bulanan", cashFlow.getLabel());
            assertEquals(400000, cashFlow.getAmount());
            assertEquals("Menerima gaji bulanan dari perusahaan.", cashFlow.getDescription());
        }

        // Skenario 2: CashFlow dengan nilai default
        {
            CashFlow cashFlow = new CashFlow();

            assertNull(cashFlow.getId());
            assertNull(cashFlow.getUserId()); // userId juga harus null
            assertNull(cashFlow.getType());
            assertNull(cashFlow.getSource());
            assertNull(cashFlow.getLabel());
            assertNull(cashFlow.getAmount());
            assertNull(cashFlow.getDescription());
            assertNull(cashFlow.getCreatedAt());
            assertNull(cashFlow.getUpdatedAt());
        }

        // Skenario 3: CashFlow dengan Setter
        {
            CashFlow cashFlow = new CashFlow();
            UUID generatedId = UUID.randomUUID();
            UUID generatedUserId = UUID.randomUUID(); // UUID untuk user

            cashFlow.setId(generatedId);
            cashFlow.setUserId(generatedUserId); // Set userId
            cashFlow.setType("Set Type");
            cashFlow.setSource("Set Source");
            cashFlow.setLabel("Set Label");
            cashFlow.setAmount(500000);
            cashFlow.setDescription("Set Description");
            
            // Simulasi PrePersist dan PreUpdate
            cashFlow.onCreate();
            cashFlow.onUpdate();

            assertEquals(generatedId, cashFlow.getId());
            assertEquals(generatedUserId, cashFlow.getUserId()); // Verifikasi userId
            assertEquals("Set Type", cashFlow.getType());
            assertEquals("Set Source", cashFlow.getSource());
            assertEquals("Set Label", cashFlow.getLabel());
            assertEquals(500000, cashFlow.getAmount());
            assertEquals("Set Description", cashFlow.getDescription());
            
            assertNotNull(cashFlow.getCreatedAt());
            assertNotNull(cashFlow.getUpdatedAt());
        }
    }
}