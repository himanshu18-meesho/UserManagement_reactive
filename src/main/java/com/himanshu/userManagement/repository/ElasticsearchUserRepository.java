package com.himanshu.userManagement.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.himanshu.userManagement.model.User;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;


@Repository
public class ElasticsearchUserRepository implements UserRepositoryInterface{
    @Autowired
    private ElasticsearchClient client;
    

    @Override
    public Mono<User> save(User user) {
        Mono<User> userMono = Mono.just(user);
        return userMono.flatMap(usr->{
            try{
                IndexResponse response = client.index(i->i.index("users").id(usr.getId().toString()).document(usr));
                // response here was like  : 
                // response: IndexResponse: {"_id":"2018","_index":"users","_primary_term":6,"result":"created","_seq_no":8,"_shards":{"failed":0.0,"successful":1.0,"total":2.0},"_version":1}
                
                // System.out.println("--------------------------------");
                // System.out.println(response.result().toString());
                // System.out.println("--------------------------------");
                return response.result().toString() == "Created" ? Mono.just(usr) : Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save user"));
            } catch(Exception e){
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<User> findById(Integer id) {
        // return Mono.just(new User());
        try{
            GetResponse<User> response = client.get(i->i.index("users").id(id.toString()), User.class);
            // response will be something like this : -----------------------------------------------------------
            // response: GetResponse: {"_index":"users","found":true,"_id":"2000","_primary_term":6,"_seq_no":7,"_source":"User(id=2000, name=HImanshuGupta, email=himanshu101@gmail.com, password=password123)","_version":1}
            return response.found() ? Mono.just(response.source()) : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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
        // need have a check if the user with this id even exists or not? 
        if(!userExists(id).block()){
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        try{
            DeleteResponse response = client.delete(i->i.index("users").id(id.toString()));
            
            // System.out.println("--------------------------------");
            // System.out.println(response);
            // System.out.println("--------------------------------");

            // DeleteResponse was somthing like  : ------------------------------------------------------------
            // DeleteResponse: {"_id":"2013","_index":"users","_primary_term":6,"result":"deleted","_seq_no":14,"_shards":{"failed":0.0,"successful":1.0,"total":2.0},"_version":2}
            
            return response.result().toString() == "Deleted" ? Mono.just(true) : Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user"));
        } catch(Exception e){
            return Mono.error(e);
        }
    }

    @Override
    public Mono<User> updateUser(Integer id, User newUser) {
        // return Mono.just(newUser);
        if(!userExists(id).block()){
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        try{
            UpdateResponse<User> response = client.update(i->i.index("users").id(id.toString()).doc(newUser), User.class);
            return response.result().toString() == "Updated" ? Mono.just(newUser) : Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user"));
        } catch(Exception e){
            return Mono.error(e);
        }
    }

    @Override
    public Mono<Boolean> userExists(Integer id) {
        return findById(id).map(usr->true).switchIfEmpty(Mono.just(false));
    }
}
