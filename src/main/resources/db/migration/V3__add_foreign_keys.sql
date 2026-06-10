-- V3__add_remaining_foreign_keys.sql

ALTER TABLE settlements
ADD CONSTRAINT fk_settlements_creator
FOREIGN KEY (creator_id)
REFERENCES creators(id)
ON DELETE CASCADE;

ALTER TABLE sale_records
ADD CONSTRAINT fk_sale_records_course
FOREIGN KEY (course_id)
REFERENCES courses(id)
ON DELETE RESTRICT;

ALTER TABLE cancellation_records
ADD CONSTRAINT fk_cancellation_records_sale_record
FOREIGN KEY (sale_record_id)
REFERENCES sale_records(id)
ON DELETE RESTRICT;