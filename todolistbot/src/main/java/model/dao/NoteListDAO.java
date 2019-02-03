package main.java.model.dao;


import main.java.model.entity.NoteList;
import main.java.model.entity.User;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class NoteListDAO implements INoteListDAO{

    private SessionFactory sessionFactory;

    @Autowired
    public NoteListDAO(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void add(NoteList noteList) {
        sessionFactory.getCurrentSession().save(noteList);
    }

    @Override
    public void update(NoteList noteList) {
        sessionFactory.getCurrentSession().update(noteList);
    }

    @Override
    public void remove(NoteList noteList) {
        sessionFactory.getCurrentSession().delete(noteList);
    }

    @Override
    public List<NoteList> getAll() {
        return sessionFactory.getCurrentSession().createQuery("from NoteList").list();
    }

}
