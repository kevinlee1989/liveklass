import apiClient from './client'

export interface Course {
  id: string
  creatorId: string
  title: string
}

export const getCourses = (creatorId?: string) =>
  apiClient.get<Course[]>('/courses', { params: creatorId ? { creatorId } : undefined }).then((r) => r.data)

export const createCourse = (data: { id: string; creatorId: string; title: string }) =>
  apiClient.post<{ id: string }>('/courses', data).then((r) => r.data)

export const updateCourseTitle = (courseId: string, title: string) =>
  apiClient.patch<Course>(`/courses/${courseId}/title`, { title }).then((r) => r.data)

export const deleteCourse = (courseId: string) =>
  apiClient.delete(`/courses/${courseId}`)
