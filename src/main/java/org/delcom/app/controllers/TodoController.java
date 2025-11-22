package org.delcom.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Todo;
import org.delcom.app.entities.User;
import org.delcom.app.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoService todoService;

    @Autowired
    protected AuthContext authContext;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // Menambahkan todo baru
    // -------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, UUID>>> createTodo(@RequestBody Todo reqTodo) {

        if (reqTodo.getTitle() == null || reqTodo.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data title tidak valid", null));
        } else if (reqTodo.getDescription() == null || reqTodo.getDescription().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data description tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Todo newTodo = todoService.createTodo(authUser.getId(), reqTodo.getTitle(), reqTodo.getDescription());
        return ResponseEntity.ok(new ApiResponse<Map<String, UUID>>(
                "success",
                "Todo berhasil dibuat",
                Map.of("id", newTodo.getId())));
    }

    // Mendapatkan semua todo dengan opsi pencarian
    // -------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Todo>>>> getAllTodos(
            @RequestParam(required = false) String search) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<Todo> todos = todoService.getAllTodos(authUser.getId(), search);
        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Daftar todo berhasil diambil",
                Map.of("todos", todos)));
    }

    // Mendapatkan todo berdasarkan ID
    // -------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Todo>>> getTodoById(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Todo todo = todoService.getTodoById(authUser.getId(), id);
        if (todo == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data todo tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data todo berhasil diambil",
                Map.of("todo", todo)));
    }

    // Memperbarui todo berdasarkan ID
    // -------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> updateTodo(@PathVariable UUID id, @RequestBody Todo reqTodo) {

        if (reqTodo.getTitle() == null || reqTodo.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data title tidak valid", null));
        } else if (reqTodo.getDescription() == null || reqTodo.getDescription().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data description tidak valid", null));
        } else if (reqTodo.isFinished() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data isFinished tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Todo updatedTodo = todoService.updateTodo(authUser.getId(), id, reqTodo.getTitle(), reqTodo.getDescription(),
                reqTodo.isFinished());
        if (updatedTodo == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data todo tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>("success", "Data todo berhasil diperbarui", null));
    }

    // Menghapus todo berdasarkan ID
    // -------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTodo(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        boolean status = todoService.deleteTodo(authUser.getId(), id);
        if (!status) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data todo tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data todo berhasil dihapus",
                null));
    }
}