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

-- Create publication_interaction table for likes and dislikes
CREATE TABLE IF NOT EXISTS publication_interaction (
    id INT PRIMARY KEY AUTO_INCREMENT,
    publication_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    type_interaction ENUM('LIKE', 'DISLIKE') NOT NULL,
    date_interaction DATETIME NOT NULL,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    UNIQUE KEY unique_interaction (publication_id, utilisateur_id)
);

-- Create publication_report table
CREATE TABLE IF NOT EXISTS publication_report (
    id INT PRIMARY KEY AUTO_INCREMENT,
    publication_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    raison VARCHAR(255) NOT NULL,
    date_report DATETIME NOT NULL,
    statut ENUM('EN_ATTENTE', 'TRAITE', 'REJETE') DEFAULT 'EN_ATTENTE',
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
); 