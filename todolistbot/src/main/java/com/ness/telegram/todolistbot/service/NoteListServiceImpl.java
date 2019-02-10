package com.ness.telegram.todolistbot.service;

import com.ness.telegram.todolistbot.model.NoteList;
import com.ness.telegram.todolistbot.model.User;
import com.ness.telegram.todolistbot.repository.NoteListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NoteListServiceImpl implements NoteListService {

    @Autowired
    private NoteListRepository repository;

    @Override
    public List<NoteList> getAllByUser(User user) {
        return repository.getAllByUser(user);
    }

    @Override
    public void save(NoteList noteList) {
        repository.save(noteList);
    }

    @Override
    public void delete(NoteList noteList) {
        repository.delete(noteList);
    }

    @Override
    public Optional<NoteList> findById(Long id) {
        return repository.findById(id);
    }
}
