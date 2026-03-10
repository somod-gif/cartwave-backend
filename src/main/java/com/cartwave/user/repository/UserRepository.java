package com.cartwave.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cartwave.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false")
    @Override
    Optional<User> findById(UUID id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deleted = false")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deleted = false")
    List<User> findByRole(@Param("role") com.cartwave.user.entity.UserRole role);

    long countByDeletedFalse();

    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.deleted = false")
    Optional<User> findByPasswordResetToken(@Param("token") String token);

    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.deleted = false")
    Optional<User> findByEmailVerificationToken(@Param("token") String token);

}
