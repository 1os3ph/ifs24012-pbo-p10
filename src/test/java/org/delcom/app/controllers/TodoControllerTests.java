package org.delcom.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient; // PENTING: Import lenient
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Todo;
import org.delcom.app.entities.User;
import org.delcom.app.services.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class TodoControllerTests {

    @Mock
    private TodoService todoService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private TodoController todoController;

    private User mockUser;
    private Todo mockTodo;
    private UUID userId;
    private UUID todoId;

    @BeforeEach
    void setUp() {
        // === PERBAIKAN 1: MANUAL INJECTION ===
        // Karena @InjectMocks kadang gagal menginjeksi field @Autowired jika ada constructor,
        // kita masukkan manual. Karena 'authContext' protected dan satu package, kita bisa akses langsung.
        todoController.authContext = authContext; 

        userId = UUID.randomUUID();
        todoId = UUID.randomUUID();

        mockUser = new User("Test User", "test@example.com", "password");
        mockUser.setId(userId);

        mockTodo = new Todo(userId, "Belajar Spring Boot", "Deskripsi", false);
        mockTodo.setId(todoId);

        // === PERBAIKAN 2: LENIENT STUBBING ===
        // Gunakan lenient() agar Mockito tidak error "UnnecessaryStubbing" 
        // saat test validasi (judul kosong) tidak memanggil authContext.
        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        lenient().when(authContext.getAuthUser()).thenReturn(mockUser);
    }

    @Test
    @DisplayName("Test Create Todo - Success")
    void testCreateTodoSuccess() {
        when(todoService.createTodo(eq(userId), any(String.class), any(String.class)))
                .thenReturn(mockTodo);

        Todo reqTodo = new Todo(null, "Judul Baru", "Deskripsi Baru", false);
        
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = todoController.createTodo(reqTodo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(todoId, response.getBody().getData().get("id"));
    }

    @Test
    @DisplayName("Test Create Todo - Validation Failed")
    void testCreateTodoValidation() {
        // Case: Judul Kosong (authContext tidak akan dipanggil disini, aman karena lenient)
        Todo invalidTodo = new Todo(null, "", "Deskripsi", false);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = todoController.createTodo(invalidTodo);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Case: Deskripsi Kosong
        invalidTodo = new Todo(null, "Judul", null, false);
        response = todoController.createTodo(invalidTodo);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test Get All Todos")
    void testGetAllTodos() {
        when(todoService.getAllTodos(eq(userId), any())).thenReturn(List.of(mockTodo));

        ResponseEntity<ApiResponse<Map<String, List<Todo>>>> response = todoController.getAllTodos(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().get("todos").size());
    }

    @Test
    @DisplayName("Test Get Todo By ID - Found")
    void testGetTodoByIdFound() {
        when(todoService.getTodoById(userId, todoId)).thenReturn(mockTodo);

        ResponseEntity<ApiResponse<Map<String, Todo>>> response = todoController.getTodoById(todoId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(todoId, response.getBody().getData().get("todo").getId());
    }

    @Test
    @DisplayName("Test Get Todo By ID - Not Found")
    void testGetTodoByIdNotFound() {
        UUID randomId = UUID.randomUUID();
        when(todoService.getTodoById(userId, randomId)).thenReturn(null);

        ResponseEntity<ApiResponse<Map<String, Todo>>> response = todoController.getTodoById(randomId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Test Update Todo - Success")
    void testUpdateTodoSuccess() {
        Todo updateReq = new Todo(null, "Updated Title", "Updated Desc", true);
        
        when(todoService.updateTodo(eq(userId), eq(todoId), any(), any(), any()))
                .thenReturn(mockTodo);

        ResponseEntity<ApiResponse<Todo>> response = todoController.updateTodo(todoId, updateReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Test Update Todo - Validation Failed")
    void testUpdateTodoValidation() {
        Todo invalidReq = new Todo(null, "Title", "Desc", null); 
        
        ResponseEntity<ApiResponse<Todo>> response = todoController.updateTodo(todoId, invalidReq);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test Delete Todo - Success")
    void testDeleteTodoSuccess() {
        when(todoService.deleteTodo(userId, todoId)).thenReturn(true);

        ResponseEntity<ApiResponse<String>> response = todoController.deleteTodo(todoId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Test Delete Todo - Not Found")
    void testDeleteTodoNotFound() {
        when(todoService.deleteTodo(userId, todoId)).thenReturn(false);

        ResponseEntity<ApiResponse<String>> response = todoController.deleteTodo(todoId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    @DisplayName("Test Authentication Failed (403)")
    void testAuthFailed() {
        // Timpa settingan lenient di setUp dengan settingan strict khusus untuk test ini
        when(authContext.isAuthenticated()).thenReturn(false);
        
        ResponseEntity<ApiResponse<Map<String, List<Todo>>>> response = todoController.getAllTodos(null);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("fail", response.getBody().getStatus());
    }
}