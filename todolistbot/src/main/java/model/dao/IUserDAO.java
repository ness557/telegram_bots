package main.java.model.dao;

import main.java.model.entity.User;

public interface IUserDAO extends IGenericDAO<User> {
    User getByChatId(long chat_id);
}
