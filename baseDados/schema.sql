
-- USERS

CREATE TABLE teacher (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    max_daily_hours INTEGER DEFAULT 3,
    session_duration_minutes INTEGER DEFAULT 60,
    max_participants_per_session INTEGER DEFAULT 5
);

-- PARTICIPANTS

CREATE TABLE student (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    max_daily_sessions INTEGER DEFAULT 1
);

-- AVAILABILITY

CREATE TYPE owner_type AS ENUM (
    'TEACHER',
    'STUDENT'
);

CREATE TABLE availability (
    id UUID PRIMARY KEY,
    owner_name STRING NOT NULL,
    owner_type owner_type NOT NULL,
    day_of_week INTEGER CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- TIMESLOTS

CREATE TABLE timeslots (
    id UUID PRIMARY KEY,
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
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status schedule_status DEFAULT 'CREATED',

    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- SESSIONS

CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL,
    timeslot_id UUID NOT NULL,
    max_capacity INTEGER DEFAULT 5,

    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (timeslot_id) REFERENCES timeslots(id)
);

-- SESSION STUDENTS

CREATE TABLE session_students (
    session_id UUID NOT NULL,
    student_id UUID NOT NULL,

    PRIMARY KEY (session_id, student_id),

    FOREIGN KEY (session_id) REFERENCES sessions(id),
    FOREIGN KEY (student_id) REFERENCES students(id)
);