CREATE KEYSPACE IF NOT EXISTS teams WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

CREATE TABLE teams.teams(
    id UUID,
    data BLOB,
    PRIMARY KEY (id)
);