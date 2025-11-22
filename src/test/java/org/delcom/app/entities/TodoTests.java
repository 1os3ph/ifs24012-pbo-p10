package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TodoTests {

    @Test
    @DisplayName("Membuat instance dari kelas Todo")
    void testMembuatInstanceTodo() throws Exception {
        
        // Skenario 1: Todo dengan constructor lengkap
        {
            UUID uid = UUID.randomUUID();
            Todo todo = new Todo(uid, "Testing Title", "Testing Description", false);

            assertEquals(uid, todo.getUserId());
            assertEquals("Testing Title", todo.getTitle());
            assertEquals("Testing Description", todo.getDescription());
            assertFalse(todo.isFinished()); // Menggunakan assertFalse untuk boolean
        }

        // Skenario 2: Todo yang sudah selesai (isFinished = true)
        {
            UUID uid = UUID.randomUUID();
            Todo todo = new Todo(uid, "Another Title", "Another Description", true);

            assertEquals("Another Title", todo.getTitle());
            assertEquals("Another Description", todo.getDescription());
            assertTrue(todo.isFinished()); // Menggunakan assertTrue
        }

        // Skenario 3: Todo dengan nilai default (Constructor kosong)
        {
            Todo todo = new Todo();

            assertNull(todo.getId());
            assertNull(todo.getTitle());
            assertNull(todo.getDescription());
            
            // Di Todo.java, isFinished di-set default = false, bukan null.
            // private Boolean isFinished = false;
            assertFalse(todo.isFinished()); 
        }

        // Skenario 4: Todo dengan Setter
        {
            Todo todo = new Todo();
            UUID generatedId = UUID.randomUUID();
            UUID generatedUserId = UUID.randomUUID(); // Tambahan untuk setUserId

            todo.setId(generatedId);
            todo.setUserId(generatedUserId);
            todo.setTitle("Set Title");
            todo.setDescription("Set Description");
            todo.setFinished(true);
            todo.setCover("/cover.png");
            
            // Method ini protected, tapi bisa diakses karena package-nya sama
            todo.onCreate(); 
            todo.onUpdate();

            assertEquals(generatedId, todo.getId());
            assertEquals(generatedUserId, todo.getUserId());
            assertEquals("Set Title", todo.getTitle());
            assertEquals("Set Description", todo.getDescription());
            assertTrue(todo.isFinished());
            assertEquals("/cover.png", todo.getCover());
            
            // Pastikan tanggal terisi
            assertNotNull(todo.getCreatedAt());
            assertNotNull(todo.getUpdatedAt());
        }
    }
}