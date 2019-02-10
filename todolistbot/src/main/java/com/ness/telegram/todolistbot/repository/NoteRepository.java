package com.ness.telegram.todolistbot.repository;

import com.ness.telegram.todolistbot.model.Note;
import com.ness.telegram.todolistbot.model.NoteList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByNoteList(NoteList noteList);
}
