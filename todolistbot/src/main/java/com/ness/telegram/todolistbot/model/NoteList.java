package com.ness.telegram.todolistbot.model;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"user", "notes"})
@EqualsAndHashCode(exclude = {"user", "notes"})
@Entity
@Table(name = "note_list")
public class NoteList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "notelist_generator", sequenceName = "notelist_seq", allocationSize = 10)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "noteList", cascade = CascadeType.ALL)
    private List<Note> notes;
}
