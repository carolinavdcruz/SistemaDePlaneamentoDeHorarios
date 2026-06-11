-- Apagar tabelas existentes
DROP TABLE IF EXISTS "studentRestrictions" CASCADE;
DROP TABLE IF EXISTS restrictions CASCADE;
DROP TABLE IF EXISTS availability CASCADE;
DROP TABLE IF EXISTS timeslots CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS teacher CASCADE;

-- ==========================
-- Teacher
-- ==========================
CREATE TABLE teacher (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- ==========================
-- Student
-- ==========================
CREATE TABLE student (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    teacher_id INTEGER REFERENCES teacher(id),
    max_daily_sessions INTEGER DEFAULT 1
);

-- ==========================
-- Time Slots
-- ==========================
CREATE TABLE timeslots (
    id SERIAL PRIMARY KEY,
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- ==========================
-- Availability
-- ==========================
CREATE TABLE availability (
    id SERIAL PRIMARY KEY,
    teacher_id INTEGER REFERENCES teacher(id),
    student_id INTEGER REFERENCES student(id),
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    CHECK (
        (teacher_id IS NOT NULL AND student_id IS NULL)
        OR
        (teacher_id IS NULL AND student_id IS NOT NULL)
    )
);

-- ==========================
-- Teacher Restrictions
-- ==========================
CREATE TABLE restrictions (
    id SERIAL PRIMARY KEY,
    teacher_id INTEGER NOT NULL UNIQUE REFERENCES teacher(id),

    max_daily_hours INTEGER NOT NULL,
    session_duration_minutes INTEGER NOT NULL,
    max_participants_per_session INTEGER NOT NULL,
    max_sessions_per_student_per_day INTEGER NOT NULL
);

-- ==========================
-- Student Restrictions
-- ==========================
CREATE TABLE "studentRestrictions" (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES student(id),
    weekly_hours INTEGER NOT NULL
);