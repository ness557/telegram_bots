package com.ness.telegram.todolistbot.service;

import com.ness.telegram.todolistbot.model.Note;
import com.ness.telegram.todolistbot.model.NoteList;
import com.ness.telegram.todolistbot.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository repository;

    @Override
    public List<Note> findAllByNoteList(NoteList noteList) {
        return repository.findAllByNoteList(noteList);
    }

    @Override
    public void save(Note note) {
        repository.save(note);
    }

    @Override
    public void delete(Note note) {
        repository.delete(note);
    }

    @Override
    public Optional<Note> findById(Long id) {
        return repository.findById(id);
    }
}
