package com.kindcircle.repository;

import com.kindcircle.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByTokenAndTokenType(String token, EmailVerificationToken.TokenType tokenType);
    void deleteByUserId(Long userId);
    List<EmailVerificationToken> findByUserId(Long userId);
}
