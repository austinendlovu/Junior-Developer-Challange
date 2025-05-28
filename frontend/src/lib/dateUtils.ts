
import { format, parseISO, formatDistanceToNow, isValid, differenceInMinutes, differenceInHours, differenceInDays } from 'date-fns';

// Parse backend LocalDateTime format (handles nanoseconds)
export const parseBackendDate = (dateString: string): Date => {
  try {
    // Remove nanoseconds if present (everything after the last dot in seconds)
    const cleanedDateString = dateString.replace(/(\.\d{3})\d*/, '$1');
    return parseISO(cleanedDateString);
  } catch (error) {
    console.warn('Failed to parse date:', dateString, error);
    return new Date();
  }
};

// Format date for display
export const formatDisplayDate = (date: Date | string): string => {
  const parsedDate = typeof date === 'string' ? parseBackendDate(date) : date;
  
  if (!isValid(parsedDate)) {
    return 'Invalid date';
  }
  
  return format(parsedDate, 'MMM dd, yyyy');
};

// Format time for display
export const formatDisplayTime = (date: Date | string): string => {
  const parsedDate = typeof date === 'string' ? parseBackendDate(date) : date;
  
  if (!isValid(parsedDate)) {
    return 'Invalid time';
  }
  
  return format(parsedDate, 'HH:mm');
};

// Format relative time ("2 hours ago", "just now")
export const formatRelativeTime = (date: Date | string): string => {
  const parsedDate = typeof date === 'string' ? parseBackendDate(date) : date;
  
  if (!isValid(parsedDate)) {
    return 'Invalid date';
  }
  
  return formatDistanceToNow(parsedDate, { addSuffix: true });
};

// Get time until lesson
export const getTimeUntilLesson = (lesson: { date: string; startTime: string }): string => {
  try {
    const lessonDateTime = parseISO(`${lesson.date}T${lesson.startTime}`);
    const now = new Date();
    const diffMinutes = differenceInMinutes(lessonDateTime, now);
    const diffHours = differenceInHours(lessonDateTime, now);
    const diffDays = differenceInDays(lessonDateTime, now);

    if (diffMinutes < 60) {
      return `${diffMinutes} min`;
    } else if (diffHours < 24) {
      return `${diffHours} hours`;
    } else {
      return `${diffDays} days`;
    }
  } catch (error) {
    console.warn('Failed to calculate time until lesson:', error);
    return 'Invalid date';
  }
};

// Group notifications by time periods
export const groupNotificationsByTime = (notifications: any[]) => {
  const now = new Date();
  const today = [];
  const yesterday = [];
  const thisWeek = [];
  const older = [];

  notifications.forEach(notification => {
    const notificationDate = parseBackendDate(notification.createdAt);
    const diffDays = differenceInDays(now, notificationDate);

    if (diffDays === 0) {
      today.push(notification);
    } else if (diffDays === 1) {
      yesterday.push(notification);
    } else if (diffDays <= 7) {
      thisWeek.push(notification);
    } else {
      older.push(notification);
    }
  });

  return { today, yesterday, thisWeek, older };
};

// Common date format constants
export const DATE_FORMATS = {
  DISPLAY: 'MMM dd, yyyy',
  TIME: 'HH:mm',
  FULL: 'MMM dd, yyyy HH:mm',
  ISO: 'yyyy-MM-dd'
};
