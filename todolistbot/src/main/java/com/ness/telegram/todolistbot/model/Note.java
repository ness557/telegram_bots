package com.ness.telegram.todolistbot.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "noteList")
@EqualsAndHashCode(exclude = "noteList")
@Entity
@Table(name = "note")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "note_generator", sequenceName = "note_seq", allocationSize = 10)
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "is_done")
    private boolean isDone;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "note_list_id")
    private NoteList noteList;
}
