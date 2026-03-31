
DROP TABLE IF EXISTS responses       CASCADE;
DROP TABLE IF EXISTS test_attempts   CASCADE;
DROP TABLE IF EXISTS test_questions  CASCADE;
DROP TABLE IF EXISTS tests           CASCADE;
DROP TABLE IF EXISTS questions       CASCADE;
DROP TABLE IF EXISTS users           CASCADE;


CREATE TABLE users (
    user_id    SERIAL PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,          
    role       VARCHAR(10)  NOT NULL DEFAULT 'student'
                            CHECK (role IN ('student','admin')),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


CREATE TABLE questions (
    question_id   SERIAL PRIMARY KEY,
    question_text TEXT         NOT NULL,
    option_a      VARCHAR(300) NOT NULL,
    option_b      VARCHAR(300) NOT NULL,
    option_c      VARCHAR(300) NOT NULL,
    option_d      VARCHAR(300) NOT NULL,
    correct_option CHAR(1)     NOT NULL CHECK (correct_option IN ('A','B','C','D')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


CREATE TABLE tests (
    test_id    SERIAL PRIMARY KEY,
    test_name  VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE test_questions (
    test_id     INT NOT NULL REFERENCES tests(test_id)     ON DELETE CASCADE,
    question_id INT NOT NULL REFERENCES questions(question_id) ON DELETE CASCADE,
    PRIMARY KEY (test_id, question_id)
);


CREATE TABLE test_attempts (
    attempt_id   SERIAL PRIMARY KEY,
    student_id   INT         NOT NULL REFERENCES users(user_id)  ON DELETE CASCADE,
    test_id      INT         NOT NULL REFERENCES tests(test_id)  ON DELETE CASCADE,
    score        INT         CHECK (score >= 0),
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);


CREATE TABLE responses (
    response_id     SERIAL PRIMARY KEY,
    attempt_id      INT    NOT NULL REFERENCES test_attempts(attempt_id) ON DELETE CASCADE,
    question_id     INT    NOT NULL REFERENCES questions(question_id)    ON DELETE CASCADE,
    selected_option CHAR(1) CHECK (selected_option IN ('A','B','C','D')),
    UNIQUE (attempt_id, question_id)   
);

CREATE INDEX idx_users_email          ON users(email);
CREATE INDEX idx_attempts_student     ON test_attempts(student_id);
CREATE INDEX idx_attempts_test        ON test_attempts(test_id);
CREATE INDEX idx_responses_attempt    ON responses(attempt_id);
CREATE INDEX idx_tq_test              ON test_questions(test_id);


CREATE OR REPLACE FUNCTION compute_score()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_total   INT;
    v_correct INT;
BEGIN
   
    SELECT COUNT(*) INTO v_total
    FROM test_questions tq
    JOIN test_attempts  ta ON ta.test_id = tq.test_id
    WHERE ta.attempt_id = NEW.attempt_id;

   
    SELECT COUNT(*) INTO v_correct
    FROM responses r
    JOIN questions q ON q.question_id = r.question_id
    WHERE r.attempt_id      = NEW.attempt_id
      AND r.selected_option = q.correct_option;

  
    UPDATE test_attempts
    SET score = v_correct
    WHERE attempt_id = NEW.attempt_id;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_auto_score
AFTER INSERT OR UPDATE ON responses
FOR EACH ROW EXECUTE FUNCTION compute_score();


CREATE OR REPLACE VIEW v_leaderboard AS
SELECT
    u.full_name,
    t.test_name,
    ta.score,
    (SELECT COUNT(*) FROM test_questions tq WHERE tq.test_id = ta.test_id) AS total,
    ta.attempted_at
FROM test_attempts ta
JOIN users  u ON u.user_id  = ta.student_id
JOIN tests  t ON t.test_id  = ta.test_id
ORDER BY ta.score DESC, ta.attempted_at;


CREATE OR REPLACE VIEW v_question_difficulty AS
SELECT
    q.question_id,
    LEFT(q.question_text, 60) AS question_preview,
    COUNT(r.response_id)      AS attempts,
    SUM(CASE WHEN r.selected_option = q.correct_option THEN 1 ELSE 0 END) AS correct_count,
    ROUND(
        100.0 * SUM(CASE WHEN r.selected_option = q.correct_option THEN 1 ELSE 0 END)
        / NULLIF(COUNT(r.response_id), 0), 1
    ) AS accuracy_pct
FROM questions q
LEFT JOIN responses r ON r.question_id = q.question_id
GROUP BY q.question_id, q.question_text
ORDER BY accuracy_pct ASC;


INSERT INTO users(full_name, email, password, role)
VALUES ('Administrator', 'admin@test.com', 'admin123', 'admin');


INSERT INTO users(full_name, email, password, role)
VALUES ('Test Student', 'student@test.com', 'student123', 'student');


INSERT INTO tests(test_name) VALUES ('General Knowledge Test');


INSERT INTO questions(question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
('What is the capital of France?',               'Berlin','Madrid','Paris','Rome',      'C'),
('Which planet is known as the Red Planet?',     'Venus','Mars','Jupiter','Saturn',     'B'),
('What is 12 × 12?',                             '132','144','156','168',               'B'),
('Who wrote Romeo and Juliet?',                  'Dickens','Austen','Shakespeare','Poe','C'),
('What is the chemical symbol for water?',       'H2O','CO2','O2','NaCl',               'A'),
('Which is the largest ocean on Earth?',         'Atlantic','Indian','Arctic','Pacific', 'D'),
('What is the square root of 144?',              '10','11','12','13',                   'C'),
('Which country invented the World Wide Web?',   'USA','UK','Germany','Japan',          'B'),
('How many bones are in the human body?',        '196','206','216','226',               'B'),
('What is the speed of light (approx)?',         '3×10^6 m/s','3×10^7 m/s','3×10^8 m/s','3×10^9 m/s','C');


INSERT INTO test_questions(test_id, question_id)
SELECT 1, question_id FROM questions;