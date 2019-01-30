package com.ness.telegram.wishlistbot.repository;

import javax.transaction.Transactional;
import com.ness.telegram.wishlistbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    User findByChatId(Integer chatId);
}