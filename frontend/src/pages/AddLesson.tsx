
import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { CalendarIcon, Clock, BookOpen, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useToast } from '@/hooks/use-toast';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Calendar } from '@/components/ui/calendar';
import { format } from 'date-fns';
import { cn } from '@/lib/utils';
import { DATE_FORMATS } from '@/lib/dateUtils';

const AddLesson = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [subject, setSubject] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState<Date>();
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [classroom, setClassroom] = useState('');
  const [type, setType] = useState('');
  const [status, setStatus] = useState('SCHEDULED');
  const [isLoading, setIsLoading] = useState(false);

  const lessonTypes = [
    { value: 'LECTURE', label: 'Lecture' },
    { value: 'PRACTICAL', label: 'Practical' },
    { value: 'LAB', label: 'Lab' },
    { value: 'SEMINAR', label: 'Seminar' },
    { value: 'TUTORIAL', label: 'Tutorial' }
  ];

  const lessonStatuses = [
    { value: 'SCHEDULED', label: 'Scheduled' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'CANCELLED', label: 'Cancelled' }
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!subject || !date || !startTime || !endTime || !classroom || !type) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive"
      });
      return;
    }

    setIsLoading(true);
    try {
      const token = localStorage.getItem('teacher_token');
      if (!token) {
        toast({
          title: "Error",
          description: "Please login first",
          variant: "destructive"
        });
        navigate('/');
        return;
      }

      const lessonData = {
        subject,
        description,
        date: format(date, DATE_FORMATS.ISO),
        startTime,
        endTime,
        classroom,
        type,
        status
      };

      const response = await fetch('http://localhost:8080/api/lessons', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(lessonData)
      });

      const data = await response.json();

      if (response.ok) {
        toast({
          title: "Lesson scheduled!",
          description: `${subject} has been added to your schedule.`,
        });
        navigate('/dashboard');
      } else {
        throw new Error(data.message || 'Failed to create lesson');
      }
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to schedule lesson",
        variant: "destructive"
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <Button 
          variant="ghost" 
          size="sm"
          onClick={() => navigate('/dashboard')}
          className="hover:bg-gray-100"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Dashboard
        </Button>
      </div>

      <div className="max-w-2xl mx-auto">
        <Card className="shadow-lg border-0">
          <CardHeader className="text-center pb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 rounded-2xl mb-4 mx-auto">
              <BookOpen className="w-8 h-8 text-blue-600" />
            </div>
            <CardTitle className="text-2xl font-bold">Schedule New Lesson</CardTitle>
            <CardDescription className="text-lg">
              Add a new lesson to your teaching schedule
            </CardDescription>
          </CardHeader>
          
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Subject */}
              <div className="space-y-2">
                <Label htmlFor="subject" className="text-base font-medium">
                  Subject *
                </Label>
                <Input
                  id="subject"
                  placeholder="e.g., Mathematics, Physics, Chemistry"
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  className="h-12 text-base rounded-lg"
                />
              </div>

              {/* Description */}
              <div className="space-y-2">
                <Label htmlFor="description" className="text-base font-medium">
                  Description
                </Label>
                <Textarea
                  id="description"
                  placeholder="Brief description of the lesson topic..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="min-h-20 text-base rounded-lg resize-none"
                />
              </div>

              {/* Lesson Type and Status */}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label className="text-base font-medium">Lesson Type *</Label>
                  <Select value={type} onValueChange={setType}>
                    <SelectTrigger className="h-12 text-base rounded-lg">
                      <SelectValue placeholder="Select lesson type" />
                    </SelectTrigger>
                    <SelectContent>
                      {lessonTypes.map((lessonType) => (
                        <SelectItem key={lessonType.value} value={lessonType.value}>
                          {lessonType.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label className="text-base font-medium">Status</Label>
                  <Select value={status} onValueChange={setStatus}>
                    <SelectTrigger className="h-12 text-base rounded-lg">
                      <SelectValue placeholder="Select status" />
                    </SelectTrigger>
                    <SelectContent>
                      {lessonStatuses.map((lessonStatus) => (
                        <SelectItem key={lessonStatus.value} value={lessonStatus.value}>
                          {lessonStatus.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Date */}
              <div className="space-y-2">
                <Label className="text-base font-medium">Date *</Label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      className={cn(
                        "w-full h-12 justify-start text-left font-normal rounded-lg",
                        !date && "text-muted-foreground"
                      )}
                    >
                      <CalendarIcon className="mr-3 h-4 w-4" />
                      {date ? format(date, "PPP") : <span>Pick a date</span>}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={date}
                      onSelect={setDate}
                      initialFocus
                      className="pointer-events-auto"
                    />
                  </PopoverContent>
                </Popover>
              </div>

              {/* Time Fields */}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="startTime" className="text-base font-medium">
                    Start Time *
                  </Label>
                  <div className="relative">
                    <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <Input
                      id="startTime"
                      type="time"
                      value={startTime}
                      onChange={(e) => setStartTime(e.target.value)}
                      className="h-12 pl-10 text-base rounded-lg"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="endTime" className="text-base font-medium">
                    End Time *
                  </Label>
                  <div className="relative">
                    <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <Input
                      id="endTime"
                      type="time"
                      value={endTime}
                      onChange={(e) => setEndTime(e.target.value)}
                      className="h-12 pl-10 text-base rounded-lg"
                    />
                  </div>
                </div>
              </div>

              {/* Classroom */}
              <div className="space-y-2">
                <Label htmlFor="classroom" className="text-base font-medium">
                  Classroom *
                </Label>
                <Input
                  id="classroom"
                  placeholder="e.g., Room 201, Lab 1, Auditorium"
                  value={classroom}
                  onChange={(e) => setClassroom(e.target.value)}
                  className="h-12 text-base rounded-lg"
                />
              </div>

              {/* Submit Button */}
              <div className="pt-4">
                <Button 
                  type="submit" 
                  className="w-full h-12 bg-blue-600 hover:bg-blue-700 text-base font-medium rounded-lg"
                  disabled={isLoading}
                >
                  {isLoading ? "Scheduling..." : "Schedule Lesson"}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AddLesson;