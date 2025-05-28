import React, { useState, useEffect, ReactNode } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Clock, BookOpen, Calendar, Bell, Plus, Edit, Trash2, CheckCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useToast } from '@/hooks/use-toast';
import { format, isToday, parseISO, differenceInHours, differenceInMinutes } from 'date-fns';
import { parseBackendDate, formatRelativeTime, getTimeUntilLesson, formatDisplayTime } from '@/lib/dateUtils';

interface Lesson {
  id: number;
  subject: string;
  description: string;
  date: string;
  startTime: string;
  endTime: string;
  classroom: string;
  type: string;
  status?: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
}

interface Notification {
  date: any;
  startTime: any;
  classroom: ReactNode;
  subject: ReactNode;
  id: number;
  lessonId: number;
  message: string;
  type: string;
  createdAt: string;
  lesson: {
    subject: string;
    startTime: string;
    classroom: string;
    date: string;
  };
}

const Dashboard = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const teacherName = localStorage.getItem('teacher_name') || 'Teacher';
  
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [weeklyLessons, setWeeklyLessons] = useState<Lesson[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Fetch notifications
  const fetchNotifications = async () => {
    try {
      const token = localStorage.getItem('teacher_token');
      if (!token) return;

      const response = await fetch('http://localhost:8080/api/lessons/notifications', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log(data)
        setNotifications(data);
        
        // Show toast notifications for immediate lessons
        data.forEach((notification: Notification) => {
          const lessonDateTime = parseISO(`${notification.lesson.date}T${notification.lesson.startTime}`);
          const diffMinutes = differenceInMinutes(lessonDateTime, new Date());
          
          if (diffMinutes <= 10 && diffMinutes > 0) {
            toast({
              title: "Lesson starting soon!",
              description: `${notification.lesson.subject} starts in ${diffMinutes} minutes in ${notification.lesson.classroom}`,
              variant: "default"
            });
          } else if (diffMinutes <= 30 && diffMinutes > 10) {
            toast({
              title: "Upcoming lesson reminder",
              description: `${notification.lesson.subject} starts in ${diffMinutes} minutes in ${notification.lesson.classroom}`,
              variant: "default"
            });
          }
        });
      }
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };

  // Fetch all lessons
  const fetchLessons = async () => {
    try {
      const token = localStorage.getItem('teacher_token');
      if (!token) {
        navigate('/');
        return;
      }

      const response = await fetch('http://localhost:8080/api/lessons', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        setLessons(data || []);
      } else {
        throw new Error('Failed to fetch lessons');
      }
    } catch (error) {
      console.error('Error fetching lessons:', error);
      toast({
        title: "Error",
        description: "Failed to fetch lessons",
        variant: "destructive"
      });
    }
  };

  // Fetch weekly lessons
  const fetchWeeklyLessons = async () => {
    try {
      const token = localStorage.getItem('teacher_token');
      if (!token) return;

      // Get the start of current week (Monday)
      const today = new Date();
      const currentDay = today.getDay();
      const mondayOffset = currentDay === 0 ? -6 : 1 - currentDay;
      const monday = new Date(today);
      monday.setDate(today.getDate() + mondayOffset);
      const weekStartDate = format(monday, 'yyyy-MM-dd');

      const response = await fetch(`http://localhost:8080/api/lessons/week?weekStartDate=${weekStartDate}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        setWeeklyLessons(data || []);
      }
    } catch (error) {
      console.error('Error fetching weekly lessons:', error);
    }
  };

  // Update lesson status
  const updateLessonStatus = async (lessonId: number, status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED') => {
    try {
      const token = localStorage.getItem('teacher_token');
      if (!token) return;

      const response = await fetch(`http://localhost:8080/api/lessons/${lessonId}/status`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status })
      });

      if (response.ok) {
        toast({
          title: "Success",
          description: `Lesson marked as ${status.toLowerCase()}`
        });
        // Refresh lessons
        fetchLessons();
        fetchWeeklyLessons();
      } else {
        throw new Error('Failed to update lesson status');
      }
    } catch (error) {
      console.error('Error updating lesson status:', error);
      toast({
        title: "Error",
        description: "Failed to update lesson status",
        variant: "destructive"
      });
    }
  };

  // Delete lesson
  const deleteLessonHandler = async (lessonId: number) => {
    try {
      const token = localStorage.getItem('teacher_token');
      if (!token) return;

      const response = await fetch(`http://localhost:8080/api/lessons/${lessonId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      console.log("hhhh",response)

      if (response.ok) {
        toast({
          title: "Success",
          description: "Lesson deleted successfully"
        });
        // Refresh lessons
        fetchLessons();
        fetchWeeklyLessons();
      } else {
        throw new Error('Failed to delete lesson');
      }
    } catch (error) {
      console.error('Error deleting lesson:', error?.message);
      toast({
        title: "Error",
        description: "Failed to delete lesson",
        variant: "destructive"
      });
    }
  };

  useEffect(() => {
    const loadData = async () => {
      setIsLoading(true);
      await Promise.all([fetchLessons(), fetchWeeklyLessons(), fetchNotifications()]);
      setIsLoading(false);
    };
    loadData();

    // Set up interval for checking notifications every minute
    const interval = setInterval(fetchNotifications, 60000);
    return () => clearInterval(interval);
  }, []);

  // Filter today's lessons with null check
  const todayLessons = lessons.filter(lesson => lesson && lesson.date && isToday(parseISO(lesson.date)));
  
  // Get upcoming lessons (today and future) with proper null checks
  const upcomingLessons = lessons
    .filter(lesson => {
      if (!lesson || !lesson.date) return false;
      const lessonDate = parseISO(lesson.date);
      return lessonDate >= new Date() || isToday(lessonDate);
    })
    .sort((a, b) => {
      if (!a || !b || !a.date || !b.date || !a.startTime || !b.startTime) return 0;
      const dateA = parseISO(`${a.date}T${a.startTime}`);
      const dateB = parseISO(`${b.date}T${b.startTime}`);
      return dateA.getTime() - dateB.getTime();
    })
    .slice(0, 5); // Show only next 5 lessons

  // Get next lesson
  const nextLesson = upcomingLessons[0];

  // Calculate time until next lesson
  const getTimeUntilLesson = (lesson: Lesson) => {
    if (!lesson || !lesson.date || !lesson.startTime) return 'Invalid date';
    
    const lessonDateTime = parseISO(`${lesson.date}T${lesson.startTime}`);
    const now = new Date();
    const diffHours = differenceInHours(lessonDateTime, now);
    const diffMinutes = differenceInMinutes(lessonDateTime, now);

    if (diffMinutes < 60) {
      return `${diffMinutes} min`;
    } else if (diffHours < 24) {
      return `${diffHours} hours`;
    } else {
      const days = Math.floor(diffHours / 24);
      return `${days} days`;
    }
  };

  // Calculate stats
  const todayStats = {
    totalLessons: todayLessons.length,
    completed: todayLessons.filter(lesson => {
      if (!lesson || !lesson.date || !lesson.endTime) return false;
      const lessonEndTime = parseISO(`${lesson.date}T${lesson.endTime}`);
      return lessonEndTime < new Date();
    }).length,
    upcoming: todayLessons.filter(lesson => {
      if (!lesson || !lesson.date || !lesson.startTime) return false;
      const lessonStartTime = parseISO(`${lesson.date}T${lesson.startTime}`);
      return lessonStartTime > new Date();
    }).length,
    nextLesson: nextLesson ? `${nextLesson.subject} at ${nextLesson.startTime}` : 'No upcoming lessons'
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading your schedule...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Welcome Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Welcome back, {teacherName}! 👋
          </h1>
          <p className="text-gray-600">
            Here's what's happening with your schedule today.
          </p>
        </div>
        <Button 
          onClick={() => navigate('/add-lesson')}
          className="bg-blue-600 hover:bg-blue-700 mt-4 md:mt-0"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Lesson
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Today's Lessons
            </CardTitle>
            <BookOpen className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">{todayStats.totalLessons}</div>
            <p className="text-xs text-gray-600 mt-1">
              {todayStats.completed} completed, {todayStats.upcoming} upcoming
            </p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Next Lesson
            </CardTitle>
            <Clock className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">
              {nextLesson ? nextLesson.startTime : '--:--'}
            </div>
            <p className="text-xs text-gray-600 mt-1">
              {nextLesson ? `${nextLesson.subject} - ${nextLesson.classroom}` : 'No upcoming lessons'}
            </p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              This Week
            </CardTitle>
            <Calendar className="h-4 w-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">{weeklyLessons.length}</div>
            <p className="text-xs text-gray-600 mt-1">
              Total lessons scheduled
            </p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Notifications
            </CardTitle>
            <Bell className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">{notifications.length}</div>
            <p className="text-xs text-gray-600 mt-1">
              Total notifications
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Upcoming Lessons */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center">
              <Calendar className="w-5 h-5 mr-2 text-blue-600" />
              Upcoming Lessons
            </CardTitle>
            <CardDescription>
              Your next scheduled classes
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {upcomingLessons.length > 0 ? (
              upcomingLessons.map((lesson) => {
                if (!lesson) return null;
                
                return (
                  <div 
                    key={lesson.id}
                    className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:shadow-md transition-shadow"
                  >
                    <div className="flex-1">
                      <div className="flex items-center space-x-3">
                        <div className={`w-3 h-3 rounded-full ${lesson.status === 'COMPLETED' ? 'bg-green-600' : lesson.status === 'CANCELLED' ? 'bg-red-600' : 'bg-blue-600'}`}></div>
                        <div>
                          <h3 className="font-semibold text-gray-900">{lesson.subject}</h3>
                          <p className="text-sm text-gray-600">{lesson.description}</p>
                          <div className="flex items-center space-x-4 mt-1">
                            <span className="text-sm text-gray-500 flex items-center">
                              <Clock className="w-3 h-3 mr-1" />
                              {lesson.startTime} - {lesson.endTime}
                            </span>
                            <span className="text-sm text-gray-500">
                              {lesson.classroom}
                            </span>
                            {lesson.date && (
                              <span className="text-sm text-gray-500">
                                {format(parseISO(lesson.date), 'MMM dd')}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2 ml-4">
                      <div className="flex items-center space-x-2">
                        <label className="text-sm text-gray-600">Complete</label>
                        <Switch
                          checked={lesson.status === 'COMPLETED'}
                          onCheckedChange={(checked) => updateLessonStatus(lesson.id, checked ? 'COMPLETED' : 'SCHEDULED')}
                        />
                      </div>
                      <Badge variant="secondary">
                        in {getTimeUntilLesson(lesson)}
                      </Badge>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => deleteLessonHandler(lesson.id)}
                        className="text-red-600 hover:text-red-700 hover:bg-red-50"
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="text-center py-8">
                <Calendar className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500 mb-4">No upcoming lessons</p>
                <Button 
                  onClick={() => navigate('/add-lesson')}
                  variant="outline"
                >
                  Schedule Your First Lesson
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Notifications */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <div className="flex items-center">
                <Bell className="w-5 h-5 mr-2 text-orange-600" />
                Notifications
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => navigate('/notifications')}
                className="text-blue-600 hover:text-blue-700"
              >
                View All
              </Button>
            </CardTitle>
            <CardDescription>
              Recent updates and reminders
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
  {notifications.length > 0 ? (
    notifications.slice(0, 5).map(notification => {
      // Append ':00' for full ISO time format if not already present
      const rawDate = notification.date; // "YYYY-MM-DD"
      const rawTime = notification.startTime; // "HH:mm"
      const fullTime = rawTime.includes(':') && rawTime.split(':').length === 2
        ? `${rawTime}:00`
        : rawTime;

      const fullDateTime = `${rawDate}T${fullTime}`; // "2025-05-28T14:30:00"
      const lessonDateTime = parseISO(fullDateTime);
      const diffMinutes = differenceInMinutes(lessonDateTime, new Date());

      const isUrgent = diffMinutes <= 10 && diffMinutes > 0;
      const isUpcoming = diffMinutes <= 30 && diffMinutes > 10;
      const notificationTime = parseBackendDate(notification.createdAt);

      return (
        <div
          key={notification.id}
          className={`flex items-start space-x-3 p-3 rounded-lg border ${
            isUrgent
              ? 'bg-red-50 border-red-200'
              : isUpcoming
              ? 'bg-yellow-50 border-yellow-200'
              : 'bg-blue-50 border-blue-200'
          }`}
        >
          <div
            className={`w-2 h-2 rounded-full mt-2 ${
              isUrgent
                ? 'bg-red-600'
                : isUpcoming
                ? 'bg-yellow-600'
                : 'bg-blue-600'
            }`}
          ></div>
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-900">
              {notification.subject} - {notification.classroom}
            </p>
            <p className="text-xs text-gray-600 mt-1">
              Lesson: {formatDisplayTime(lessonDateTime)} on {format(lessonDateTime, 'MMM dd')}
            </p>
            <p className="text-xs text-gray-400 mt-1">
              {formatRelativeTime(notificationTime)}
            </p>
          </div>
        </div>
      );
    })
  ) : (
    <div className="text-center py-4">
      <Bell className="w-8 h-8 text-gray-400 mx-auto mb-2" />
      <p className="text-sm text-gray-500">No notifications</p>
    </div>
  )}
</CardContent>

        </Card>
      </div>
    </div>
  );
};

export default Dashboard;