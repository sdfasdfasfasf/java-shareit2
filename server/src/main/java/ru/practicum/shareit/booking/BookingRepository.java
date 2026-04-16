package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Для списка бронирований пользователя
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // Для владельца
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // Для комментария – поиск завершённого бронирования
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.item.id = :itemId AND b.status = :status AND b.end <= :now")
    Optional<Booking> findCompletedBooking(@Param("bookerId") Long bookerId,
                                           @Param("itemId") Long itemId,
                                           @Param("status") BookingStatus status,
                                           @Param("now") LocalDateTime now);

    // Для отладки
    List<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);

    // Для дат бронирований в вещах
    List<Booking> findByItemIdAndStatusAndStartBefore(Long itemId, BookingStatus status, LocalDateTime start, Sort sort);

    List<Booking> findByItemIdAndStatusAndStartAfter(Long itemId, BookingStatus status, LocalDateTime start, Sort sort);
}