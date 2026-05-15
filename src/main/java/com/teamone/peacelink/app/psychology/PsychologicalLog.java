//package com.teamone.peacelink.app.psychology;
//
//import com.teamone.peacelink.app.user.User;
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "psychological_logs")
//@Getter @Setter
//@NoArgsConstructor @AllArgsConstructor @Builder
//public class PsychologicalLog {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    private Integer anxietyScore;
//
//    @Column(columnDefinition = "TEXT")
//    private String userMessage;
//
//    @Column(columnDefinition = "TEXT")
//    private String aiResponse;
//
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//    }
//}