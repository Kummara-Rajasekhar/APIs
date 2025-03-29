package com.example.loadbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import javax.persistence.*;
import java.util.*;

@SpringBootApplication
public class LoadBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoadBookingApplication.class, args);
    }
}

@Entity
class Load {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String shipperId;
    private String loadingPoint;
    private String unloadingPoint;
    private Date loadingDate;
    private Date unloadingDate;
    private String productType;
    private String truckType;
    private int noOfTrucks;
    private double weight;
    private String comment;
    private Date datePosted = new Date();
    private String status = "POSTED";

    // Getters and Setters
}

@Entity
class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID loadId;
    private String transporterId;
    private double proposedRate;
    private String comment;
    private String status = "PENDING";
    private Date requestedAt = new Date();

    // Getters and Setters
}

interface LoadRepository extends JpaRepository<Load, UUID> {}
interface BookingRepository extends JpaRepository<Booking, UUID> {}

@RestController
@RequestMapping("/load")
class LoadController {
    @Autowired
    private LoadRepository loadRepository;

    @PostMapping
    public ResponseEntity<Load> createLoad(@RequestBody Load load) {
        return new ResponseEntity<>(loadRepository.save(load), HttpStatus.CREATED);
    }

    @GetMapping
    public List<Load> getAllLoads() {
        return loadRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Load> getLoadById(@PathVariable UUID id) {
        return loadRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Load> updateLoad(@PathVariable UUID id, @RequestBody Load load) {
        if (!loadRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        load.setId(id);
        return ResponseEntity.ok(loadRepository.save(load));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoad(@PathVariable UUID id) {
        loadRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

@RestController
@RequestMapping("/booking")
class BookingController {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private LoadRepository loadRepository;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Load load = loadRepository.findById(booking.getLoadId()).orElse(null);
        if (load == null || "CANCELLED".equals(load.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        load.setStatus("BOOKED");
        loadRepository.save(load);
        return new ResponseEntity<>(bookingRepository.save(booking), HttpStatus.CREATED);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable UUID id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable UUID id, @RequestBody Booking booking) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        booking.setId(id);
        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            Load load = loadRepository.findById(booking.getLoadId()).orElse(null);
            if (load != null) {
                load.setStatus("CANCELLED");
                loadRepository.save(load);
            }
            bookingRepository.deleteById(id);
        }
        return ResponseEntity.noContent().build();
    }
}
