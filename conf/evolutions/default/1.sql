# --- First database schema

# --- !Ups

set ignorecase true;

create table budgie (
  id                        bigint not null,
  name                      varchar(255) not null,
  colour                    varchar(255) not null,
  picture                   varchar(255),
  race                      varchar(255) not null,
  gender                    char not null
);


create sequence budgie_seq start with 17;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists budgie;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists budgie_seq;

