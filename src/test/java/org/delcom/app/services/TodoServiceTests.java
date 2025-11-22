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
        // 1. Persiapan Data
        UUID userId = UUID.randomUUID();
        UUID todoId = UUID.randomUUID();
        UUID nonexistentTodoId = UUID.randomUUID();

        Todo todo = new Todo(userId, "Belajar Spring Boot", "Belajar Unit Test", false);
        todo.setId(todoId);

        // ==========================================
        // TEST 1: createTodo
        // ==========================================
        {
            when(todoRepository.save(any(Todo.class))).thenReturn(todo);
            
            Todo createdTodo = todoService.createTodo(userId, "Belajar Spring Boot", "Belajar Unit Test");

            assertNotNull(createdTodo);
            assertEquals(userId, createdTodo.getUserId());
            assertEquals("Belajar Spring Boot", createdTodo.getTitle());
        }

        // ==========================================
        // TEST 2: getAllTodos (Tanpa Search / NULL)
        // ==========================================
        {
            when(todoRepository.findAll()).thenReturn(List.of(todo));

            List<Todo> result = todoService.getAllTodos(userId, null);

            assertEquals(1, result.size());
            assertEquals(todoId, result.get(0).getId());
        }

        // ==========================================
        // TEST 3: getAllTodos (Dengan Search Valid)
        // ==========================================
        {
            String keyword = "Belajar";
            when(todoRepository.findByKeyword(eq(userId), eq(keyword))).thenReturn(List.of(todo));

            List<Todo> result = todoService.getAllTodos(userId, keyword);

            assertEquals(1, result.size());
            assertEquals("Belajar Spring Boot", result.get(0).getTitle());
        }

        // ==========================================
        // TEST 3.5: getAllTodos (Search Kosong/Spasi) 
        // ==========================================
        {
            String emptyKeyword = "   "; // Tidak null, tapi kosong
            
            // Logika service: jika kosong, dia harus panggil findAll(), bukan findByKeyword
            // Kita re-stub findAll karena ini skenario baru
            when(todoRepository.findAll()).thenReturn(List.of(todo));

            List<Todo> result = todoService.getAllTodos(userId, emptyKeyword);

            assertEquals(1, result.size());
            // Verifikasi findAll dipanggil lagi (total 2x: saat null dan saat empty string)
            verify(todoRepository, times(2)).findAll();
        }

        // ==========================================
        // TEST 4: getTodoById (Sukses & Gagal)
        // ==========================================
        {
            when(todoRepository.findByUserIdAndId(userId, todoId)).thenReturn(Optional.of(todo));
            when(todoRepository.findByUserIdAndId(userId, nonexistentTodoId)).thenReturn(Optional.empty());

            // Kasus Ada
            Todo found = todoService.getTodoById(userId, todoId);
            assertNotNull(found);

            // Kasus Tidak Ada
            Todo notFound = todoService.getTodoById(userId, nonexistentTodoId);
            assertNull(notFound);
        }

        // ==========================================
        // TEST 5: updateTodo (Sukses)
        // ==========================================
        {
            when(todoRepository.findByUserIdAndId(userId, todoId)).thenReturn(Optional.of(todo));
            when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Todo updated = todoService.updateTodo(userId, todoId, "Judul Baru", "Deskripsi Baru", true);

            assertNotNull(updated);
            assertEquals("Judul Baru", updated.getTitle());
            assertEquals("Deskripsi Baru", updated.getDescription());
            assertTrue(updated.isFinished());
        }

        // ==========================================
        // TEST 6: updateTodo (Gagal / Not Found)
        // ==========================================
        {
            when(todoRepository.findByUserIdAndId(userId, nonexistentTodoId)).thenReturn(Optional.empty());

            Todo result = todoService.updateTodo(userId, nonexistentTodoId, "Judul", "Desc", true);

            assertNull(result);
        }

        // ==========================================
        // TEST 7: deleteTodo
        // ==========================================
        {
            // Case Sukses
            when(todoRepository.findByUserIdAndId(userId, todoId)).thenReturn(Optional.of(todo));
            boolean isDeleted = todoService.deleteTodo(userId, todoId);
            assertTrue(isDeleted);
            verify(todoRepository, times(1)).deleteById(todoId);

            // Case Gagal (ID Salah)
            when(todoRepository.findByUserIdAndId(userId, nonexistentTodoId)).thenReturn(Optional.empty());
            boolean failedDelete = todoService.deleteTodo(userId, nonexistentTodoId);
            assertFalse(failedDelete);
        }
    }
}