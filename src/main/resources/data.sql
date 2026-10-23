-- Вставляем пользователей
INSERT INTO users (name) VALUES ('Иван Иванов') ON CONFLICT DO NOTHING;
INSERT INTO users (name) VALUES ('Петр Петров') ON CONFLICT DO NOTHING;

-- Вставляем парковочные зоны
INSERT INTO parking_zones (capacity, hourly_rate) VALUES (10, 150.00) ON CONFLICT DO NOTHING;
INSERT INTO parking_zones (capacity, hourly_rate) VALUES (5, 300.00) ON CONFLICT DO NOTHING;

-- Вставляем автомобили (привязка к пользователям id=1, id=2)
INSERT INTO vehicles (license_plate, user_id) VALUES ('А123ВС77', 1) ON CONFLICT DO NOTHING;
INSERT INTO vehicles (license_plate, user_id) VALUES ('В456КХ99', 2) ON CONFLICT DO NOTHING;