package com.ness.telegram.todolistbot.repository;

import com.ness.telegram.todolistbot.model.NoteList;
import com.ness.telegram.todolistbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteListRepository extends JpaRepository<NoteList, Long> {
    List<NoteList> getAllByUser(User user);
}
