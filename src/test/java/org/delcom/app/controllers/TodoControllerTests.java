package org.delcom.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
        // Manual Injection untuk AuthContext
        todoController.authContext = authContext;

        userId = UUID.randomUUID();
        todoId = UUID.randomUUID();

        mockUser = new User("Test User", "test@example.com", "password");
        mockUser.setId(userId);

        mockTodo = new Todo(userId, "Belajar Spring Boot", "Deskripsi", false);
        mockTodo.setId(todoId);

        // Default: User Login (lenient agar tidak error di test auth failed)
        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        lenient().when(authContext.getAuthUser()).thenReturn(mockUser);
    }

    // ==========================================
    // 1. TEST CREATE TODO
    // ==========================================
    @Test
    @DisplayName("Create Todo - Success")
    void testCreateTodoSuccess() {
        when(todoService.createTodo(eq(userId), any(String.class), any(String.class)))
                .thenReturn(mockTodo);

        Todo reqTodo = new Todo(null, "Judul Baru", "Deskripsi Baru", false);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = todoController.createTodo(reqTodo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Create Todo - Validation Error (Title/Desc Empty)")
    void testCreateTodoValidation() {
        // Case 1: Title Null
        Todo invalidTodo = new Todo(null, null, "Deskripsi", false);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.createTodo(invalidTodo).getStatusCode());

        // Case 2: Title Empty
        invalidTodo = new Todo(null, "", "Deskripsi", false);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.createTodo(invalidTodo).getStatusCode());

        // Case 3: Description Null
        invalidTodo = new Todo(null, "Judul", null, false);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.createTodo(invalidTodo).getStatusCode());

        // Case 4: Description Empty
        invalidTodo = new Todo(null, "Judul", "", false);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.createTodo(invalidTodo).getStatusCode());
    }

    @Test
    @DisplayName("Create Todo - Auth Failed (403)")
    void testCreateTodoUnauthenticated() {
        // Simulasi belum login
        when(authContext.isAuthenticated()).thenReturn(false);
        
        Todo reqTodo = new Todo(null, "Judul", "Desc", false);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = todoController.createTodo(reqTodo);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()); // Menargetkan baris merah Auth di createTodo
    }

    // ==========================================
    // 2. TEST GET ALL TODOS
    // ==========================================
    @Test
    @DisplayName("Get All Todos - Success")
    void testGetAllTodos() {
        when(todoService.getAllTodos(eq(userId), any())).thenReturn(List.of(mockTodo));
        ResponseEntity<ApiResponse<Map<String, List<Todo>>>> response = todoController.getAllTodos(null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get All Todos - Auth Failed (403)")
    void testGetAllTodosUnauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, List<Todo>>>> response = todoController.getAllTodos(null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ==========================================
    // 3. TEST GET TODO BY ID
    // ==========================================
    @Test
    @DisplayName("Get Todo By ID - Success")
    void testGetTodoByIdFound() {
        when(todoService.getTodoById(userId, todoId)).thenReturn(mockTodo);
        ResponseEntity<ApiResponse<Map<String, Todo>>> response = todoController.getTodoById(todoId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Todo By ID - Not Found (404)")
    void testGetTodoByIdNotFound() {
        when(todoService.getTodoById(userId, todoId)).thenReturn(null);
        ResponseEntity<ApiResponse<Map<String, Todo>>> response = todoController.getTodoById(todoId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Todo By ID - Auth Failed (403)")
    void testGetTodoByIdUnauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, Todo>>> response = todoController.getTodoById(todoId);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()); // Menargetkan baris merah Auth di getTodoById
    }

    // ==========================================
    // 4. TEST UPDATE TODO
    // ==========================================
    @Test
    @DisplayName("Update Todo - Success")
    void testUpdateTodoSuccess() {
        Todo updateReq = new Todo(null, "Updated Title", "Updated Desc", true);
        when(todoService.updateTodo(eq(userId), eq(todoId), any(), any(), any())).thenReturn(mockTodo);

        ResponseEntity<ApiResponse<Todo>> response = todoController.updateTodo(todoId, updateReq);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Todo - Validation Error (Bad Request)")
    void testUpdateTodoValidation() {
        // Menargetkan if (reqTodo.getTitle() == null...)
        Todo invalidReq1 = new Todo(null, null, "Desc", true);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.updateTodo(todoId, invalidReq1).getStatusCode());

        // Menargetkan if (...isEmpty())
        Todo invalidReq2 = new Todo(null, "", "Desc", true);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.updateTodo(todoId, invalidReq2).getStatusCode());

        // Menargetkan else if (reqTodo.getDescription() == null...)
        Todo invalidReq3 = new Todo(null, "Title", null, true);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.updateTodo(todoId, invalidReq3).getStatusCode());

         // Menargetkan else if (...isEmpty())
         Todo invalidReq4 = new Todo(null, "Title", "", true);
         assertEquals(HttpStatus.BAD_REQUEST, todoController.updateTodo(todoId, invalidReq4).getStatusCode());

        // Menargetkan else if (reqTodo.isFinished() == null)
        Todo invalidReq5 = new Todo(null, "Title", "Desc", null);
        assertEquals(HttpStatus.BAD_REQUEST, todoController.updateTodo(todoId, invalidReq5).getStatusCode());
    }

    @Test
    @DisplayName("Update Todo - Not Found (404)")
    void testUpdateTodoNotFound() {
        Todo updateReq = new Todo(null, "Title", "Desc", true);
        
        // Simulasi Service mengembalikan NULL (ID tidak ketemu di database)
        when(todoService.updateTodo(any(), any(), any(), any(), any())).thenReturn(null);

        ResponseEntity<ApiResponse<Todo>> response = todoController.updateTodo(todoId, updateReq);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); // Menargetkan baris merah Not Found di updateTodo
    }

    @Test
    @DisplayName("Update Todo - Auth Failed (403)")
    void testUpdateTodoUnauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        Todo updateReq = new Todo(null, "Title", "Desc", true);
        
        ResponseEntity<ApiResponse<Todo>> response = todoController.updateTodo(todoId, updateReq);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()); // Menargetkan baris merah Auth di updateTodo
    }

    // ==========================================
    // 5. TEST DELETE TODO
    // ==========================================
    @Test
    @DisplayName("Delete Todo - Success")
    void testDeleteTodoSuccess() {
        when(todoService.deleteTodo(userId, todoId)).thenReturn(true);
        ResponseEntity<ApiResponse<String>> response = todoController.deleteTodo(todoId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Delete Todo - Not Found (404)")
    void testDeleteTodoNotFound() {
        when(todoService.deleteTodo(userId, todoId)).thenReturn(false);
        ResponseEntity<ApiResponse<String>> response = todoController.deleteTodo(todoId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); // Menargetkan baris merah Not Found di deleteTodo
    }

    @Test
    @DisplayName("Delete Todo - Auth Failed (403)")
    void testDeleteTodoUnauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<String>> response = todoController.deleteTodo(todoId);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()); // Menargetkan baris merah Auth di deleteTodo
    }
}