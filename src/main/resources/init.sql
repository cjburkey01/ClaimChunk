CREATE TABLE claimchunk
(
  chunkID INT PRIMARY KEY AUTO_INCREMENT,
  world   VARCHAR(100),
  posX    INT          NOT NULL,
  posZ    INT          NOT NULL,
  ownerID VARCHAR(265) NOT NULL,
  name    VARCHAR(40)
);
CREATE UNIQUE INDEX claimchunk_chunkID_uindex
  ON claimchunk (chunkID)