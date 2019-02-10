package com.ness.telegram.todolistbot.model;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"noteLists", "currentList"})
@EqualsAndHashCode(exclude = {"noteLists", "currentList"})
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "user_generator", sequenceName = "user_seq", allocationSize = 10)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<NoteList> noteLists;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_list_id")
    private NoteList currentList;

    @Column(name = "current_statement")
    @Enumerated
    private Statement currentStatement;
}
