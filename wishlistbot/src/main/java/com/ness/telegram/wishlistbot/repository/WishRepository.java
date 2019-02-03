package com.ness.telegram.wishlistbot.repository;

import java.util.List;
import javax.transaction.Transactional;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface WishRepository extends JpaRepository<Wish, Long>{
    Wish findByLabelAndUser(String label, User user);
    Wish findByLabelAndUserChatId(String label, Long chatId);
    List<Wish> findByUserChatId(Long chatId);
}