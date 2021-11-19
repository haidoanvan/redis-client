package com.codergeezer.redisclient.logic;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * @author haidv
 * @version 1.0
 */
public class Notifier {

  private final NotificationGroup REDIS_GROUP =
      NotificationGroupManager.getInstance().getNotificationGroup("Redis Notification Group");

  private final Project project;

  private Notifier(Project project) {
    this.project = project;
  }

  public static Notifier getInstance(Project project) {
    return project.getService(Notifier.class);
  }

  public void notifyInfo(String message) {
    notify(message, NotificationType.INFORMATION);
  }

  public void notifyError(String message) {
    notify(message, NotificationType.ERROR);
  }

  private void notify(String message, NotificationType notificationType) {
    REDIS_GROUP.createNotification(message, notificationType).notify(project);
  }
}
