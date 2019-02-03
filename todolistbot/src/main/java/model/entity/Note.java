package main.java.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "note")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "text")
    private String text;

    @Column(name = "is_done")
    private boolean isDone;

    @ManyToOne
    @JoinColumn(name = "note_list_id")
    private NoteList noteList;

    public Note() {
    }

    public Note(String text, boolean isDone, NoteList noteList) {
        this.text = text;
        this.isDone = isDone;
        this.noteList = noteList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public NoteList getNoteList() {
        return noteList;
    }

    public void setNoteList(NoteList noteList) {
        this.noteList = noteList;
    }
}
