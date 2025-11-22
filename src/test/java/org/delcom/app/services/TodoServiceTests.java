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

import org.delcom.app.entities.Todo;
import org.delcom.app.repositories.TodoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTests {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("Pengujian lengkap untuk TodoService")
    void testTodoService() {
        // 1. Persiapan Data (userId sangat penting di sini)
        UUID userId = UUID.randomUUID();
        UUID todoId = UUID.randomUUID();
        UUID nonexistentTodoId = UUID.randomUUID();

        // Sesuaikan constructor dengan Entity Todo kamu (ada userId di depan)
        Todo todo = new Todo(userId, "Belajar Spring Boot", "Belajar Unit Test", false);
        todo.setId(todoId);

        // ==========================================
        // TEST 1: createTodo
        // ==========================================
        {
            // Mocking: Saat save dipanggil, kembalikan objek todo yang sama
            when(todoRepository.save(any(Todo.class))).thenReturn(todo);

            // Panggil service (Wajib sertakan userId)
            Todo createdTodo = todoService.createTodo(userId, "Belajar Spring Boot", "Belajar Unit Test");

            assertNotNull(createdTodo);
            assertEquals(userId, createdTodo.getUserId());
            assertEquals("Belajar Spring Boot", createdTodo.getTitle());
        }

        // ==========================================
        // TEST 2: getAllTodos (Tanpa Search)
        // ==========================================
        {
            // Mocking findAll (sesuai logika service kamu baris 28)
            when(todoRepository.findAll()).thenReturn(List.of(todo));

            List<Todo> result = todoService.getAllTodos(userId, null);

            assertEquals(1, result.size());
            assertEquals(todoId, result.get(0).getId());
        }

        // ==========================================
        // TEST 3: getAllTodos (Dengan Search)
        // ==========================================
        {
            String keyword = "Belajar";
            // Mocking findByKeyword (sesuai logika service kamu baris 26)
            // Kita pakai eq() untuk mencocokkan argumen spesifik
            when(todoRepository.findByKeyword(eq(userId), eq(keyword))).thenReturn(List.of(todo));

            List<Todo> result = todoService.getAllTodos(userId, keyword);

            assertEquals(1, result.size());
            assertEquals("Belajar Spring Boot", result.get(0).getTitle());
        }

        // ==========================================
        // TEST 4: getTodoById (Sukses & Gagal)
        // ==========================================
        {
            // Mocking findByUserIdAndId (Bukan findById!)
            when(todoRepository.findByUserIdAndId(userId, todoId)).thenReturn(Optional.of(todo));
            when(todoRepository.findByUserIdAndId(userId, nonexistentTodoId)).thenReturn(Optional.empty());

            // Kasus Ada
            Todo found = todoService.getTodoById(userId, todoId);
            assertNotNull(found);
            assertEquals(todoId, found.getId());

            // Kasus Tidak Ada
            Todo notFound = todoService.getTodoById(userId, nonexistentTodoId);
            assertNull(notFound);
        }

        // ==========================================
        // TEST 5: updateTodo
        // ==========================================
        {
            // Mocking: Perlu return Optional berisi todo dulu agar if(todo != null) tembus
            when(todoRepository.findByUserIdAndId(userId, todoId)).thenReturn(Optional.of(todo));
            when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Todo updated = todoService.updateTodo(userId, todoId, "Judul Baru", "Deskripsi Baru", true);

            assertNotNull(updated);
            assertEquals("Judul Baru", updated.getTitle());
            assertEquals("Deskripsi Baru", updated.getDescription());
            assertTrue(updated.isFinished());
        }

        // ==========================================
        // TEST 6: deleteTodo
        // ==========================================
        {
            // Case Sukses: Mock find dulu supaya tidak return null
            when(todoRepository.findByUserIdAndId(userId, todoId)).thenReturn(Optional.of(todo));
            
            boolean isDeleted = todoService.deleteTodo(userId, todoId);
            
            assertTrue(isDeleted);
            // Verifikasi bahwa method deleteById benar-benar dipanggil 1 kali
            verify(todoRepository, times(1)).deleteById(todoId);

            // Case Gagal (ID Salah)
            when(todoRepository.findByUserIdAndId(userId, nonexistentTodoId)).thenReturn(Optional.empty());
            
            boolean failedDelete = todoService.deleteTodo(userId, nonexistentTodoId);
            assertFalse(failedDelete);
        }
    }
}