package com.group7.marketplacesystem.communication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class NotificationuserId implements Serializable {
    private static final long serialVersionUID = 7010327277904311607L;
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "notification_id", nullable = false)
    private Integer notificationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        NotificationuserId entity = (NotificationuserId) o;
        return Objects.equals(this.notificationId, entity.notificationId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, userId);
    }

}