package com.ejada.TaskManager.repository;

import com.ejada.TaskManager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {



//    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tasks WHERE u.id = :id")
//    Optional<User> findUserById(@Param("id") int id);

    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);

}
