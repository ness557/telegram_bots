package main.java.model.dao;

import main.java.model.entity.Note;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class NoteDAO implements INoteDAO {

    private SessionFactory sessionFactory;

    @Autowired
    public NoteDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void add(Note note) {
        sessionFactory.getCurrentSession().save(note);
    }

    @Override
    public void update(Note note) {
        sessionFactory.getCurrentSession().update(note);
    }

    @Override
    public void remove(Note note) {
        sessionFactory.getCurrentSession().delete(note);
    }

    @Override
    public List<Note> getAll() {
        return sessionFactory.getCurrentSession().createQuery("from Note").list();
    }
}
