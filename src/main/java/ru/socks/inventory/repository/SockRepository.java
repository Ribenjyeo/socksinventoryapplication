package ru.socks.inventory.repository;

import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.socks.inventory.model.Sock;

import java.util.List;
import java.util.UUID;

@Repository
public interface SockRepository extends JpaRepository<Sock, Long> {

    @Modifying
    @Query(value = """
                INSERT INTO socks (id, color, cotton_content, quantity)
                VALUES (nextval('sock_sequence'), :color, :cottonContent, :quantity)
                ON CONFLICT (color, cotton_content)
                DO UPDATE SET quantity = socks.quantity + :quantity
            """, nativeQuery = true)
    void incrementOrInsertSock(@Param("color") String color,
                               @Param("cottonContent") int cottonContent,
                               @Param("quantity") int quantity);

    @Modifying
    @Query(value = """
                UPDATE socks
                SET quantity = quantity - :quantity
                WHERE color = :color AND cotton_content = :cottonContent AND quantity >= :quantity
            """, nativeQuery = true)
    int decreaseStock(@Param("color") String color,
                      @Param("cottonContent") int cottonContent,
                      @Param("quantity") int quantity);

    @Modifying
    @Query(value = """
                UPDATE socks
                SET color = :color, cotton_content = :cottonContent, quantity = :quantity
                WHERE id = :id
                  AND NOT EXISTS (
                      SELECT 1
                      FROM socks
                      WHERE color = :color AND cotton_content = :cottonContent AND id != :id
                  )
            """, nativeQuery = true)
    int updateSockWithUniqueCheck(@Param("id") Long id,
                                  @Param("color") String color,
                                  @Param("cottonContent") int cottonContent,
                                  @Param("quantity") int quantity);

    List<Sock> findByCottonContentGreaterThan(int cottonContent, Sort sort);

    List<Sock> findByCottonContentLessThan(int cottonContent, Sort sort);

    List<Sock> findByCottonContent(int cottonContent, Sort sort);

    List<Sock> findByCottonContentBetween(int minCottonContent, int maxCottonContent, Sort sort);

    List<Sock> findByColorOrderByCottonContentAsc(String color);
}
