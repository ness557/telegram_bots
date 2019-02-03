package main.java.model.entity;


import main.java.model.entity.Note;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "note_list")
public class NoteList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "noteList" , fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Note> notes;

    public NoteList() {
    }

    public NoteList(String name, User user, List<Note> notes) {
        this.name = name;
        this.user = user;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}
