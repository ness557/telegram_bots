package com.ness.telegram.wishlistbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "wish")
public class Wish {

    @Id
    private Long id;

    @Column(name = "label")
    private String label;

    @Column(name = "link")
    private String link;
}