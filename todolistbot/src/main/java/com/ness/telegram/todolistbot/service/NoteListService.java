package com.ness.telegram.todolistbot.service;

import com.ness.telegram.todolistbot.model.NoteList;
import com.ness.telegram.todolistbot.model.User;

import java.util.List;
import java.util.Optional;

public interface NoteListService {
    List<NoteList> getAllByUser(User user);
    void save(NoteList noteList);
    void delete(NoteList noteList);
    Optional<NoteList> findById(Long id);
}
