package com.ness.telegram.wishlistbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
@Entity
@Table(name = "wish")
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wish_generator")
    @SequenceGenerator(name = "wish_generator", sequenceName = "wish_seq", allocationSize = 50)
    private Long id;

    @Column(name = "label", unique = true)
    private String label;

    @Column(name = "link")
    private String link;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn
    private User user;
}