
-- USERS

CREATE TABLE teacher (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    max_daily_hours INTEGER DEFAULT 3,
    session_duration_minutes INTEGER DEFAULT 60,
    max_participants_per_session INTEGER DEFAULT 5
);

-- PARTICIPANTS

CREATE TABLE student (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    max_daily_sessions INTEGER DEFAULT 1
);

-- AVAILABILITY
CREATE TABLE availability (
    id INTEGER PRIMARY KEY,
    teacher_id INTEGER,
    student_id INTEGER,

    day_of_week INTEGER CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    FOREIGN KEY (teacher_id) REFERENCES teacher(id),
    FOREIGN KEY (student_id) REFERENCES student(id),

    CHECK (
        (teacher_id IS NOT NULL AND student_id IS NULL) OR
        (teacher_id IS NULL AND student_id IS NOT NULL)
    )
);

-- TIMESLOTS

CREATE TABLE timeslots (
    id INTEGER PRIMARY KEY,
    day_of_week INTEGER CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- SCHEDULE

CREATE TYPE schedule_status AS ENUM (
    'CREATED',
    'ACCEPTED',
    'REJECTED'
);

CREATE TABLE schedules (
    id INTEGER PRIMARY KEY,
    teacher_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status schedule_status DEFAULT 'CREATED',

    FOREIGN KEY (teacher_id) REFERENCES teacher(id)
);

-- SESSIONS

CREATE TABLE sessions (
    id INTEGER PRIMARY KEY,
    schedule_id INTEGER NOT NULL,
    timeslot_id INTEGER NOT NULL,
    max_capacity INTEGER DEFAULT 5,

    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (timeslot_id) REFERENCES timeslots(id)
);

-- SESSION STUDENTS

CREATE TABLE sessions (
    id INTEGER PRIMARY KEY,
    schedule_id INTEGER NOT NULL,
    timeslot_id INTEGER NOT NULL,
    max_capacity INTEGER DEFAULT 5,

    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (timeslot_id) REFERENCES timeslots(id)
);