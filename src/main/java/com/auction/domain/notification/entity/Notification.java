package com.auction.domain.notification.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    private String content;
    private String relatedUrl;
    private boolean isRead;
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Notification(User user, String content, String relatedUrl, NotificationType type) {
        this.receiver = user;
        this.content = content;
        this.relatedUrl = relatedUrl;
        this.isRead = false;
        this.type = type;
    }
    public static Notification of(User user, String content, String relatedUrl, NotificationType type) {
        return new Notification(user, content, relatedUrl, type);
    }

    public void read() {
        this.isRead = true;
    }

    public enum NotificationType {
        AUCTION, REVIEW
    }
}
