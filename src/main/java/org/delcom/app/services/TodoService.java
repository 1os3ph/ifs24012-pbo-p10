package org.delcom.app.services;

import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Todo;
import org.delcom.app.repositories.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional
    public Todo createTodo(UUID userId, String title, String description) {
        Todo todo = new Todo(userId, title, description, false);
        return todoRepository.save(todo);
    }

    public List<Todo> getAllTodos(UUID userId, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return todoRepository.findByKeyword(userId, search);
        }
        return todoRepository.findAll();
    }

    public Todo getTodoById(UUID userId, UUID id) {
        return todoRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    @Transactional
    public Todo updateTodo(UUID userId, UUID id, String title, String description, Boolean isFinished) {
        Todo todo = todoRepository.findByUserIdAndId(userId, id).orElse(null);
        if (todo != null) {
            todo.setTitle(title);
            todo.setDescription(description);
            todo.setFinished(isFinished);
            return todoRepository.save(todo);
        }
        return null;
    }

    @Transactional
    public boolean deleteTodo(UUID userId, UUID id) {
        Todo todo = todoRepository.findByUserIdAndId(userId, id).orElse(null);
        if (todo == null) {
            return false;
        }

        todoRepository.deleteById(id);
        return true;
    }
}