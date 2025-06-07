package com.himanshu.userManagement;

import com.himanshu.userManagement.model.User;
import com.himanshu.userManagement.repository.ElasticsearchUserRepository;
import com.himanshu.userManagement.repository.RedisUserRepository;
import com.himanshu.userManagement.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private RedisUserRepository redisUserRepository = mock(RedisUserRepository.class);
    private ElasticsearchUserRepository elasticsearchUserRepository = mock(ElasticsearchUserRepository.class);
    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(elasticsearchUserRepository, redisUserRepository);
    }

    @Test
    void saveUser_ShouldSaveInRedis_WhenIdIsOdd() {
        User user = new User(1, "Himanshu1", "himanshu1@gmail.com", "Himanshu@123");
        when(redisUserRepository.save(user)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.saveUser(user))
            .expectNext(user)
            .verifyComplete();

        verify(redisUserRepository).save(user);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void saveUser_ShouldSaveInElasticsearch_WhenIdIsEven() {
        User user = new User(2, "Himanshu2", "himanshu2@gmail.com", "Himanshu@123");
        when(elasticsearchUserRepository.save(user)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.saveUser(user))
            .expectNext(user)
            .verifyComplete();

        verify(elasticsearchUserRepository).save(user);
        verifyNoInteractions(redisUserRepository);
    }

    @Test
    void saveUser_ShouldThrowBadRequest_WhenEmailIsMissing() {
        User user = new User(3, "Himanshu3", null, "Himanshu@123");

        StepVerifier.create(userService.saveUser(user))
            .expectError(ResponseStatusException.class)
            .verify();

        verifyNoInteractions(redisUserRepository);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void deleteUser_ShouldDeleteFromElastic_WhenIdIsEven() {
        when(elasticsearchUserRepository.deleteById(2)).thenReturn(Mono.just(true));

        StepVerifier.create(userService.deleteUser(2))
            .expectNext(true)
            .verifyComplete();

        verify(elasticsearchUserRepository).deleteById(2);
        verifyNoInteractions(redisUserRepository);
    }

    @Test
    void deleteUser_ShouldDeleteFromRedis_WhenIdIsOdd() {
        when(redisUserRepository.deleteById(1)).thenReturn(Mono.just(true));

        StepVerifier.create(userService.deleteUser(1))
            .expectNext(true)
            .verifyComplete();

        verify(redisUserRepository).deleteById(1);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void saveUser_ShouldThrowBadRequest_WhenPasswordIsMissing() {
        User user = new User(4, "Himanshu4", "himanshu4@gmail.com", null);

        StepVerifier.create(userService.saveUser(user))
            .expectError(ResponseStatusException.class)
            .verify();

        verifyNoInteractions(redisUserRepository);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void deleteUser_ShouldThrowBadRequest_WhenIdIsInvalid() {
        StepVerifier.create(userService.deleteUser(null))
            .expectError(ResponseStatusException.class)
            .verify();

        StepVerifier.create(userService.deleteUser(0))
            .expectError(ResponseStatusException.class)
            .verify();

        StepVerifier.create(userService.deleteUser(-1))
            .expectError(ResponseStatusException.class)
            .verify();

        verifyNoInteractions(redisUserRepository);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void findById_ShouldFindFromRedis_WhenIdIsOdd() {
        User user = new User(1, "Himanshu1", "himanshu1@gmail.com", "Himanshu@123");
        when(redisUserRepository.findById(1)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.findById(1))
            .expectNext(user)
            .verifyComplete();

        verify(redisUserRepository).findById(1);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void findById_ShouldFindFromElasticsearch_WhenIdIsEven() {
        User user = new User(2, "Himanshu2", "himanshu2@gmail.com", "Himanshu@123");
        when(elasticsearchUserRepository.findById(2)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.findById(2))
            .expectNext(user)
            .verifyComplete();

        verify(elasticsearchUserRepository).findById(2);
        verifyNoInteractions(redisUserRepository);
    }

    @Test
    void findById_ShouldThrowBadRequest_WhenIdIsInvalid() {
        StepVerifier.create(userService.findById(null))
            .expectError(ResponseStatusException.class)
            .verify();

        StepVerifier.create(userService.findById(0))
            .expectError(ResponseStatusException.class)
            .verify();

        StepVerifier.create(userService.findById(-1))
            .expectError(ResponseStatusException.class)
            .verify();

        verifyNoInteractions(redisUserRepository);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void updateUser_ShouldUpdateInRedis_WhenIdIsOdd() {
        User updatedUser = new User(1, "UpdatedName", "updated@gmail.com", "newPass@123");
        when(redisUserRepository.updateUser(1, updatedUser)).thenReturn(Mono.just(updatedUser));

        StepVerifier.create(userService.updateUser(1, updatedUser))
            .expectNext(updatedUser)
            .verifyComplete();

        verify(redisUserRepository).updateUser(1, updatedUser);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void updateUser_ShouldUpdateInElasticsearch_WhenIdIsEven() {
        User updatedUser = new User(2, "UpdatedName", "updated@gmail.com", "newPass@123");
        when(elasticsearchUserRepository.updateUser(2, updatedUser)).thenReturn(Mono.just(updatedUser));

        StepVerifier.create(userService.updateUser(2, updatedUser))
            .expectNext(updatedUser)
            .verifyComplete();

        verify(elasticsearchUserRepository).updateUser(2, updatedUser);
        verifyNoInteractions(redisUserRepository);
    }

    @Test
    void updateUser_ShouldThrowBadRequest_WhenIdIsInvalid() {
        User user = new User(1, "Test", "test@gmail.com", "pass@123");

        StepVerifier.create(userService.updateUser(null, user))
            .expectError(ResponseStatusException.class)
            .verify();

        StepVerifier.create(userService.updateUser(0, user))
            .expectError(ResponseStatusException.class)
            .verify();

        verifyNoInteractions(redisUserRepository);
        verifyNoInteractions(elasticsearchUserRepository);
    }

    @Test
    void updateUser_ShouldThrowBadRequest_WhenNewUserIsNull() {
        StepVerifier.create(userService.updateUser(1, null))
            .expectError(ResponseStatusException.class)
            .verify();

        verifyNoInteractions(redisUserRepository);
        verifyNoInteractions(elasticsearchUserRepository);
    }
}
