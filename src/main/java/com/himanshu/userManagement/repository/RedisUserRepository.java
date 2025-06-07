package com.himanshu.userManagement.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import com.himanshu.userManagement.model.User;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


@Repository
public class RedisUserRepository implements UserRepositoryInterface{
    private final ReactiveRedisTemplate<String, User> redisTemplate;
    private final ReactiveValueOperations<String, User> valueOps;

    public RedisUserRepository(ReactiveRedisTemplate<String, User> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    @Override
    public Mono<User> save(User user) {
        try{
            // System.out.println("--------------------------------");
            // valueOps.set(user.getId().toString(), user).thenReturn(user).subscribe(System.out::println);
            // System.out.println("--------------------------------");
            return valueOps.set(user.getId().toString(), user).thenReturn(user);
        } catch(Exception e){
            return Mono.error(e);
        }
    }

    @Override
    public Mono<User> findById(Integer id) {
        try{
            return valueOps.get(id.toString()).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Redis")));
        } catch(Exception e){
            return Mono.error(e);
        }
    }

    @Override
    public Flux<User> findAll() {
        return Flux.empty();
    }

    @Override
    public Mono<Boolean> deleteById(Integer id) {
        try{
            return valueOps.delete(id.toString()).thenReturn(true).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Redis")))  ;
        } catch(Exception e){
            return Mono.error(e);
        }
    }

    @Override
    public Mono<User> updateUser(Integer id, User newUser) {
        try{
            
            // System.out.println("--------------------------------");
            // valueOps.set(id.toString(), newUser).thenReturn(newUser).subscribe(System.out::println);
            // System.out.println("--------------------------------");
            return valueOps.set(id.toString(), newUser).thenReturn(newUser).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Redis")));
        } catch(Exception e){
            return Mono.error(e);
        }
    }

    @Override
    public Mono<Boolean> userExists(Integer id) {
        try{
            return valueOps.get(id.toString()).map(usr->true).switchIfEmpty(Mono.just(false));
        } catch(Exception e){
            return Mono.error(e);
        }
    }
}