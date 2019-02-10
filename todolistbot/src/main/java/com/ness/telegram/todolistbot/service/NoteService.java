package com.ness.telegram.todolistbot.service;

import com.ness.telegram.todolistbot.model.Note;
import com.ness.telegram.todolistbot.model.NoteList;

import java.util.List;
import java.util.Optional;

public interface NoteService {
    List<Note> findAllByNoteList(NoteList noteList);
    void save(Note note);
    void delete(Note note);
    Optional<Note> findById(Long id);
}
