ALTER TABLE courses
ADD CONSTRAINT fk_courses_creator
FOREIGN KEY (creator_id)
REFERENCES creators(id)
ON DELETE CASCADE;