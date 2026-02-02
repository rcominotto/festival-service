-- ========================================
-- Insert sample artists
-- ========================================

INSERT INTO artist (name, photo_url, genre) VALUES
                                                ('The Rolling Stones', 'https://example.com/rollingstones.jpg', 'Rock'),
                                                ('Beyonce', 'https://example.com/beyonce.jpg', 'Pop'),
                                                ('Daft Punk', 'https://example.com/daftpunk.jpg', 'Electronic');

-- ========================================
-- Insert sample festivals
-- ========================================

INSERT INTO festival (name, place, date, price) VALUES
                                                    ('Summer Fest', 'New York', '2026-06-21', 120.00),
                                                    ('Rock Mania', 'Los Angeles', '2026-07-15', 150.00);

-- ========================================
-- Insert festival photos
-- ========================================

INSERT INTO festival_photos (festival_id, photo_url) VALUES
                                                         (1, 'https://example.com/summerfest1.jpg'),
                                                         (1, 'https://example.com/summerfest2.jpg'),
                                                         (2, 'https://example.com/rockmania1.jpg');

-- ========================================
-- Link festivals with artists (many-to-many)
-- ========================================

-- Summer Fest lineup: Rolling Stones + Beyonce
INSERT INTO festival_artists (festival_id, artist_id) VALUES
                                                          (1, 1),
                                                          (1, 2);

-- Rock Mania lineup: Rolling Stones + Daft Punk
INSERT INTO festival_artists (festival_id, artist_id) VALUES
                                                          (2, 1),
                                                          (2, 3);