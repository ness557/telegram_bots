package main.java.model.dao;

import java.util.List;

public interface IGenericDAO<T> {

    void add(T t);
    void update(T t);
    void remove(T t);
    List<T> getAll();
}
