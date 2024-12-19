package ru.socks.inventory.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "socks")
@AllArgsConstructor
@NoArgsConstructor
public class Sock {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sock_seq")
    @SequenceGenerator(name = "sock_seq", sequenceName = "sock_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer cottonContent;

    @Column(nullable = false)
    private Integer quantity;
}
