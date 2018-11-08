create table vote (
  id integer identity primary key,
  election_id integer not null,
  voter_id integer not null,
  candidate_number integer not null
);
