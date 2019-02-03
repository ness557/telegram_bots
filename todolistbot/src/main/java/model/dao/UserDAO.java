package main.java.model.dao;

import main.java.model.entity.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class UserDAO implements IUserDAO {

    private SessionFactory sessionFactory;

    @Autowired
    public UserDAO(SessionFactory theSessionFactory) {
        sessionFactory = theSessionFactory;
    }

    @Override
    public void add(User user) {
        sessionFactory.getCurrentSession().save(user);
    }

    @Override
    public void update(User user) {
        sessionFactory.getCurrentSession().update(user);
    }

    @Override
    public void remove(User user) {
        sessionFactory.getCurrentSession().delete(user);
    }

    @Override
    public List<User> getAll() {
        return sessionFactory.getCurrentSession().createQuery("from User").list();
    }

    public User getByChatId(long id){
        return (User) sessionFactory.getCurrentSession().createQuery("from User u where u.chatId = " + id).uniqueResult();
    }
}
