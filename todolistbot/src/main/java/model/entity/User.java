package main.java.model.entity;


import main.java.model.entity.NoteList;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "chat_id")
    private long chatId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<NoteList> noteLists;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_list_id")
    private NoteList currentList;

    @Column(name = "current_statement")
    private int currentStatement;

    public User() {
    }

    public User(long chatId, List<NoteList> noteLists, NoteList currentList) {
        this.chatId = chatId;
        this.noteLists = noteLists;
        this.currentList = currentList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public List<NoteList> getNoteLists() {
        return noteLists;
    }

    public void setNoteLists(List<NoteList> noteLists) {
        this.noteLists = noteLists;
    }

    public NoteList getCurrentList() {
        return currentList;
    }

    public void setCurrentList(NoteList currentList) {
        this.currentList = currentList;
    }

    public int getCurrentStatement() {
        return currentStatement;
    }

    public void setCurrentStatement(int currentStatement) {
        this.currentStatement = currentStatement;
    }
}
