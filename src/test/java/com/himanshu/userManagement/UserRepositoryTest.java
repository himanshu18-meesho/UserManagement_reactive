package com.himanshu.userManagement;

import com.himanshu.userManagement.model.User;
import com.himanshu.userManagement.repository.RedisUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private ReactiveRedisTemplate<String, User> redisTemplate;
    
    @Mock
    private ReactiveValueOperations<String, User> valueOps;
    
    private RedisUserRepository redisUserRepository;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        redisUserRepository = new RedisUserRepository(redisTemplate);
    }

    @Test
    void redisRepository_save_ShouldReturnUser_WhenSuccessful() {
        User user = new User(1, "Himanshu", "himanshu@gmail.com", "password123");
        when(valueOps.set("1", user)).thenReturn(Mono.just(true));

        StepVerifier.create(redisUserRepository.save(user)).expectNext(user).verifyComplete();

        verify(valueOps).set("1", user);
    }

    @Test
    void redisRepository_findById_ShouldReturnUser_WhenUserExists() {
        User user = new User(1, "Himanshu", "himanshu@gmail.com", "password123");
        when(valueOps.get("1")).thenReturn(Mono.just(user));

        StepVerifier.create(redisUserRepository.findById(1)).expectNext(user).verifyComplete();

        verify(valueOps).get("1");
    }

    @Test
    void redisRepository_findById_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(valueOps.get("1")).thenReturn(Mono.empty());

        StepVerifier.create(redisUserRepository.findById(1)).expectError(ResponseStatusException.class).verify();

        verify(valueOps).get("1");
    }

    @Test
    void redisRepository_deleteById_ShouldReturnTrue_WhenSuccessful() {
        when(valueOps.delete("1")).thenReturn(Mono.just(true));

        StepVerifier.create(redisUserRepository.deleteById(1)).expectNext(true).verifyComplete();

        verify(valueOps).delete("1");
    }

    @Test
    void redisRepository_updateUser_ShouldReturnUpdatedUser_WhenSuccessful() {
        User updatedUser = new User(1, "Updated Himanshu", "himanshu.updated@gmail.com", "newpass123");
        when(valueOps.set("1", updatedUser)).thenReturn(Mono.just(true));

        StepVerifier.create(redisUserRepository.updateUser(1, updatedUser)).expectNext(updatedUser).verifyComplete();

        verify(valueOps).set("1", updatedUser);
    }

    @Test
    void redisRepository_userExists_ShouldReturnTrue_WhenUserExists() {
        User user = new User(1, "Himanshu", "himanshu@gmail.com", "password123");
        when(valueOps.get("1")).thenReturn(Mono.just(user));

        StepVerifier.create(redisUserRepository.userExists(1)).expectNext(true).verifyComplete();

        verify(valueOps).get("1");
    }

    @Test
    void redisRepository_userExists_ShouldReturnFalse_WhenUserDoesNotExist() {
        when(valueOps.get("1")).thenReturn(Mono.empty());

        StepVerifier.create(redisUserRepository.userExists(1)).expectNext(false).verifyComplete();

        verify(valueOps).get("1");
    }
}
