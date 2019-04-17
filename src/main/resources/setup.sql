CREATE TABLE IF NOT EXISTS Users(id int AUTO_INCREMENT PRIMARY KEY,
                                 username varchar(255) NOT NULL,
                                 password varchar(255) NOT NULL
                                 );

INSERT INTO Users(username, password) VALUES ('Marcus', 'HASH');
INSERT INTO Users(username, password) VALUES ('Krister', 'HASH2');
INSERT INTO Users(username, password) VALUES ('Karl', 'HASH3');