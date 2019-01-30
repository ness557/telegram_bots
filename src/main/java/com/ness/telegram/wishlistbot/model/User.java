package com.ness.telegram.wishlistbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;

@NoArgsConstructor
@Getter 
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "uzer")
public class User {

    @Id
    private Long id;

    @Column(name = "telegram_chat_id")
    private Integer chatId;
    
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state = State.DEFAULT;
}