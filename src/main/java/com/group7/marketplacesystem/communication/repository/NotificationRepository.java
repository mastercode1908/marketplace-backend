package com.group7.marketplacesystem.communication.repository;

import com.group7.marketplacesystem.communication.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

}
