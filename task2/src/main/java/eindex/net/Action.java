package eindex.net;

import java.io.Serializable;

public enum Action implements Serializable {
    LOGIN,
    ADD_ADMIN,
    ADD_STUDENT,
    ADD_SUBJECT,
    ASSIGN_STUDENT,
    UPDATE_POINTS,
    VIEW_POINTS,
    VIEW_RESULTS,
    LIST_STUDENTS,
    LIST_SUBJECTS,
    LIST_ENROLLMENTS,
    SAVE,
    SHUTDOWN
}

