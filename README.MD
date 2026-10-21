
# System Overview

Code structure follows
the [Data-Oriented Programming](https://clojureverse.org/t/review-what-is-data-oriented-programming/6065) paradigm.

## Modules

* bot - frontend for MAX users
* core - backend

## Data Entities

```mermaid
classDiagram
    class State {
        userManagement
    }
    class UserManagement {
        Map~Long, Session~ sessionsById
    }
    class Patient {
        Long phoneNumber
        Long userId
        String firstName
    }
    class Appointment {
        Long patientPhoneNumber
        OffsetDateTime startDateTime
        OffsetDateTime endDateTime
        String goal
        String message
    }
    class Session {
        String step
        AppointmentDataBuffer appointmentDataBuffer
        Appointment precreatedAppointment
    }
    class AppointmentDataBuffer { 
        String patientPhoneNumber
        String date
        String startEndTime
        String goal
        String message
    }

    State *-- UserManagement
    UserManagement *-- Admin
    UserManagement *-- Patient
    UserManagement *-- Session
    Session *-- AppointmentDataBuffer
    Appointment --> Patient
```
* `Session` is used in the process of Appointment creation / editing.

## Code Modules
Business logic written in `AppointmentManagement` and `UserManagement` modules.

```mermaid
classDiagram
    class AppointmentManagement {
            ...
        appointmentByPhoneNumber(Long phoneNumber) Appointment
    }
    class UserManagement {
        ...
        patientByPhoneNumber(Long phoneNumber) Patient
    }
```

# Multitenancy
The bot is assumed to be used in a single tenant environment. So, the state is stored in a mutable map.