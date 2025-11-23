package org.delcom.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.CashFlow;
import org.delcom.app.entities.User;
import org.delcom.app.services.CashFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class CashFlowControllerTests {

    @Mock
    private CashFlowService cashFlowService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private CashFlowController cashFlowController;

    private User mockUser;
    private CashFlow mockCashFlow;
    private UUID userId;
    private UUID cashFlowId;

    @BeforeEach
    void setUp() {
        cashFlowController.authContext = authContext;
        userId = UUID.randomUUID();
        cashFlowId = UUID.randomUUID();
        mockUser = new User("Test User", "test@example.com", "password");
        mockUser.setId(userId);
        mockCashFlow = new CashFlow(userId, "IN", "Gaji", "Bulanan", 5000, "Desc");
        mockCashFlow.setId(cashFlowId);

        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        lenient().when(authContext.getAuthUser()).thenReturn(mockUser);
    }

    static Stream<CashFlow> invalidCashFlowProvider() {
        return Stream.of(
                // Type Salah
                new CashFlow(null, null, "Src", "Lbl", 100, "Desc"),
                new CashFlow(null, "", "Src", "Lbl", 100, "Desc"),
                new CashFlow(null, "   ", "Src", "Lbl", 100, "Desc"),

                // Source Salah
                new CashFlow(null, "Type", null, "Lbl", 100, "Desc"),
                new CashFlow(null, "Type", "", "Lbl", 100, "Desc"),
                new CashFlow(null, "Type", "   ", "Lbl", 100, "Desc"),

                // Label Salah
                new CashFlow(null, "Type", "Src", null, 100, "Desc"),
                new CashFlow(null, "Type", "Src", "", 100, "Desc"),
                new CashFlow(null, "Type", "Src", "   ", 100, "Desc"),

                // Amount Salah
                new CashFlow(null, "Type", "Src", "Lbl", null, "Desc"),
                new CashFlow(null, "Type", "Src", "Lbl", 0, "Desc"),
                new CashFlow(null, "Type", "Src", "Lbl", -100, "Desc"),

                // Description Salah
                new CashFlow(null, "Type", "Src", "Lbl", 100, null),
                new CashFlow(null, "Type", "Src", "Lbl", 100, ""),
                new CashFlow(null, "Type", "Src", "Lbl", 100, "   "));
    }

    // ==========================================
    // 1. TEST CREATE (Positive & Negative)
    // ==========================================
    @Test
    @DisplayName("Create - Success")
    void testCreateSuccess() {
        when(cashFlowService.createCashFlow(any(), any(), any(), any(), any(), any())).thenReturn(mockCashFlow);
        CashFlow req = new CashFlow(null, "IN", "Src", "Lbl", 100, "Desc");
        assertEquals(HttpStatus.OK, cashFlowController.createCashFlow(req).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidCashFlowProvider")
    @DisplayName("Create - Validation Error")
    void testCreateValidation(CashFlow invalidData) {
        // Ini akan meloop semua data sampah di atas ke method Create
        assertEquals(HttpStatus.BAD_REQUEST, cashFlowController.createCashFlow(invalidData).getStatusCode());
    }

    @Test
    @DisplayName("Create - Auth Failed")
    void testCreateAuthFailed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        CashFlow req = new CashFlow(null, "IN", "Src", "Lbl", 100, "Desc");
        assertEquals(HttpStatus.FORBIDDEN, cashFlowController.createCashFlow(req).getStatusCode());
    }

    // ==========================================
    // 2. TEST UPDATE (Positive & Negative)
    // ==========================================
    @Test
    @DisplayName("Update - Success")
    void testUpdateSuccess() {
        when(cashFlowService.updateCashFlow(any(), any(), any(), any(), any(), any(), any())).thenReturn(mockCashFlow);
        CashFlow req = new CashFlow(null, "IN", "Src", "Lbl", 100, "Desc");
        assertEquals(HttpStatus.OK, cashFlowController.updateCashFlow(cashFlowId, req).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidCashFlowProvider")
    @DisplayName("Update - Validation Error")
    void testUpdateValidation(CashFlow invalidData) {
        assertEquals(HttpStatus.BAD_REQUEST,
                cashFlowController.updateCashFlow(cashFlowId, invalidData).getStatusCode());
    }

    @Test
    @DisplayName("Update - Not Found")
    void testUpdateNotFound() {
        when(cashFlowService.updateCashFlow(any(), any(), any(), any(), any(), any(), any())).thenReturn(null);
        CashFlow req = new CashFlow(null, "IN", "Src", "Lbl", 100, "Desc");
        assertEquals(HttpStatus.NOT_FOUND, cashFlowController.updateCashFlow(cashFlowId, req).getStatusCode());
    }

    @Test
    @DisplayName("Update - Auth Failed")
    void testUpdateAuthFailed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        CashFlow req = new CashFlow(null, "IN", "Src", "Lbl", 100, "Desc");
        assertEquals(HttpStatus.FORBIDDEN, cashFlowController.updateCashFlow(cashFlowId, req).getStatusCode());
    }

    // ==========================================
    // 3. TEST GET & DELETE (Standard)
    // ==========================================
    @Test
    void testGetAllSuccess() {
        when(cashFlowService.getAllCashFlows(any(), any())).thenReturn(List.of(mockCashFlow));
        assertEquals(HttpStatus.OK, cashFlowController.getAllCashFlows(null).getStatusCode());
    }

    @Test
    void testGetAllAuthFailed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, cashFlowController.getAllCashFlows(null).getStatusCode());
    }

    @Test
    void testGetByIdSuccess() {
        when(cashFlowService.getCashFlowById(any(), any())).thenReturn(mockCashFlow);
        assertEquals(HttpStatus.OK, cashFlowController.getCashFlowById(cashFlowId).getStatusCode());
    }

    @Test
    void testGetByIdNotFound() {
        when(cashFlowService.getCashFlowById(any(), any())).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND, cashFlowController.getCashFlowById(cashFlowId).getStatusCode());
    }

    @Test
    void testGetByIdAuthFailed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, cashFlowController.getCashFlowById(cashFlowId).getStatusCode());
    }

    @Test
    void testGetLabelsSuccess() {
        when(cashFlowService.getCashFlowLabels(any())).thenReturn(List.of("Gaji"));
        assertEquals(HttpStatus.OK, cashFlowController.getCashFlowLabels().getStatusCode());
    }

    @Test
    void testGetLabelsAuthFailed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, cashFlowController.getCashFlowLabels().getStatusCode());
    }

    @Test
    void testDeleteSuccess() {
        when(cashFlowService.deleteCashFlow(any(), any())).thenReturn(true);
        assertEquals(HttpStatus.OK, cashFlowController.deleteCashFlow(cashFlowId).getStatusCode());
    }

    @Test
    void testDeleteNotFound() {
        when(cashFlowService.deleteCashFlow(any(), any())).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND, cashFlowController.deleteCashFlow(cashFlowId).getStatusCode());
    }

    @Test
    void testDeleteAuthFailed() {
        when(authContext.isAuthenticated()).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, cashFlowController.deleteCashFlow(cashFlowId).getStatusCode());
    }
}