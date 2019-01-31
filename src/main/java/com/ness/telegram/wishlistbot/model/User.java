package com.ness.telegram.wishlistbot.model;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.*;

@NoArgsConstructor
@Getter 
@Setter
@ToString(exclude = "wishes")
@EqualsAndHashCode(exclude = "wishes")
@Entity
@Table(name = "uzer")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(name = "user_generator", sequenceName = "user_seq", allocationSize = 10)
    private Long id;

    @Column(name = "telegram_chat_id", unique = true)
    private Integer chatId;
    
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state = State.DEFAULT;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wish> wishes;
}