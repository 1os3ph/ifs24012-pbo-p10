package org.delcom.app.controllers;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.CashFlow;
import org.delcom.app.entities.User;
import org.delcom.app.services.CashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cash-flows")
public class CashFlowController {

    private final CashFlowService cashFlowService;

    @Autowired
    protected AuthContext authContext;

    public CashFlowController(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    private boolean isInvalid(CashFlow cf) {
        return cf.getType() == null || cf.getType().isBlank() ||
                cf.getSource() == null || cf.getSource().isBlank() ||
                cf.getLabel() == null || cf.getLabel().isBlank() ||
                cf.getAmount() == null || cf.getAmount() <= 0 ||
                cf.getDescription() == null || cf.getDescription().isBlank();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, UUID>>> createCashFlow(@RequestBody CashFlow cashFlow) {
        // Cek Login
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<Map<String, UUID>>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        // Cek Validasi
        if (isInvalid(cashFlow)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<Map<String, UUID>>("fail", "Data tidak valid", null));
        }

        CashFlow created = cashFlowService.createCashFlow(
                authUser.getId(),
                cashFlow.getType(), cashFlow.getSource(), cashFlow.getLabel(),
                cashFlow.getAmount(), cashFlow.getDescription());

        return ResponseEntity.ok(new ApiResponse<Map<String, UUID>>(
                "success",
                "Berhasil menambahkan data",
                Map.of("id", created.getId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<CashFlow>>>> getAllCashFlows(
            @RequestParam(required = false) String search) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<Map<String, List<CashFlow>>>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<CashFlow> cashFlows = cashFlowService.getAllCashFlows(authUser.getId(), search);

        return ResponseEntity.ok(new ApiResponse<Map<String, List<CashFlow>>>(
                "success",
                "Berhasil mengambil data",
                Map.of("cash_flows", cashFlows)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, CashFlow>>> getCashFlowById(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<Map<String, CashFlow>>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        CashFlow cashFlow = cashFlowService.getCashFlowById(authUser.getId(), id);
        if (cashFlow == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<Map<String, CashFlow>>("fail", "Data tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<Map<String, CashFlow>>(
                "success",
                "Berhasil mengambil data",
                Map.of("cashFlow", cashFlow)));
    }

    @GetMapping("/labels")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getCashFlowLabels() {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<Map<String, List<String>>>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<String> labels = cashFlowService.getCashFlowLabels(authUser.getId());

        return ResponseEntity.ok(new ApiResponse<Map<String, List<String>>>(
                "success",
                "Berhasil mengambil data",
                Map.of("labels", labels)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CashFlow>> updateCashFlow(@PathVariable UUID id, @RequestBody CashFlow cashFlow) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<CashFlow>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        if (isInvalid(cashFlow)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<CashFlow>("fail", "Data tidak valid", null));
        }

        CashFlow updated = cashFlowService.updateCashFlow(
                authUser.getId(), id,
                cashFlow.getType(), cashFlow.getSource(), cashFlow.getLabel(),
                cashFlow.getAmount(), cashFlow.getDescription());

        if (updated == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<CashFlow>("fail", "Gagal memperbarui data, ID tidak ditemukan", null));
        }

        // Mengembalikan data yang sudah diupdate (Best Practice) atau null jika ingin
        // hemat bandwidth
        return ResponseEntity.ok(new ApiResponse<CashFlow>("success", "Berhasil memperbarui data", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCashFlow(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<String>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        boolean deleted = cashFlowService.deleteCashFlow(authUser.getId(), id);
        if (!deleted) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<String>("fail", "Gagal menghapus data, ID tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<String>("success", "Berhasil menghapus data", null));
    }
}