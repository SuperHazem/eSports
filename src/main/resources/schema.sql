-- Create participation_event table
CREATE TABLE IF NOT EXISTS participation_event (
    id INT PRIMARY KEY AUTO_INCREMENT,
    utilisateur_id INT NOT NULL,
    event_id INT NOT NULL,
    date_participation DATETIME NOT NULL,
    confirme BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES event_social(id) ON DELETE CASCADE,
    UNIQUE KEY unique_participation (utilisateur_id, event_id)
); 