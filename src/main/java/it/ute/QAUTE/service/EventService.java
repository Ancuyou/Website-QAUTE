package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Consultant;
import it.ute.QAUTE.entity.Event;
import it.ute.QAUTE.entity.EventRegistration;
import it.ute.QAUTE.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    Page<Event> findEventsByConsultantAndFilters(Consultant consultant, Event.EventType type,
                                                 Event.EventStatus status, Pageable pageable);

    long countConsultantEventsByStatus(Consultant consultant, Event.EventStatus status);

    long countConsultantEventsByStatusIn(Consultant consultant, List<Event.EventStatus> statuses);

    @Transactional
    Event createEvent(Event event, MultipartFile bannerFile);

    @Transactional
    Event updateEvent(Integer eventId, Event updatedEvent, MultipartFile bannerFile);

    @Transactional
    void deleteEvent(Integer eventId);

    @Transactional
    Event approveEvent(Integer eventId, Integer managerId);

    @Transactional
    Event rejectEvent(Integer eventId, Integer managerId, String reason);

    @Transactional
    Event cancelEvent(Integer eventId, String reason);

    Event findById(Integer eventId);

    Page<Event> findAllEvents(Pageable pageable);

    Page<Event> findEventsByConsultant(Consultant consultant, Pageable pageable);

    Page<Event> findEventsByStatus(Event.EventStatus status, Pageable pageable);

    Page<Event> findUpcomingEvents(Pageable pageable);

    Page<Event> searchEvents(String keyword, Pageable pageable);

    Page<Event> filterEvents(
            Event.EventType type,
            Event.EventMode mode,
            Event.EventStatus status,
            Integer departmentId,
            Integer consultantId,
            Pageable pageable);

    @Transactional
    EventRegistration registerForEvent(Integer eventId, User user, String note);

    @Transactional
    void cancelRegistration(Integer registrationId, String reason);

    Page<EventRegistration> findUserRegistrations(User user, Pageable pageable);

    List<EventRegistration> findEventParticipants(Integer eventId);

    long countPendingEvents();

    long countApprovedEvents();

    long countConsultantEvents(Consultant consultant);

    void validateEventTime(Event event);

    @Transactional
    void updateEventStatuses();

    long countEventsInDateRange(LocalDateTime start, LocalDateTime end);

    long countPendingEventsInDateRange(LocalDateTime start, LocalDateTime end);

    long countApprovedEventsInDateRange(LocalDateTime start, LocalDateTime end);

    List<Event> findTop3UpcomingEvents();

    @Transactional(readOnly = true)
    Page<EventRegistration> findUserRegistrations(User user, EventRegistration.RegistrationStatus status,
                                                  Pageable pageable);

    @Transactional
    void submitFeedback(Integer registrationId, User user, Integer rating, String feedback);

    @Transactional
    void cancelRegistration(Integer registrationId, User user, String reason);

    @Transactional
    EventRegistration updateRegistrationStatus(Integer registrationId,
                                               EventRegistration.RegistrationStatus newStatus,
                                               Consultant consultant);

    @Transactional(readOnly = true)
    boolean isUserRegistered(Event event, User user);

    List<Event> findUpcomingEventsByConsultant(Consultant consultant);

    long countAll();
}
