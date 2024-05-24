CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SEQUENCE IF NOT EXISTS tc_id_seq INCREMENT 1 START 1 MINVALUE 1;
CREATE TABLE "users" (
                             "id" uuid UNIQUE PRIMARY KEY NOT NULL DEFAULT (uuid_generate_v4()),
                             "email" varchar(255) UNIQUE NOT NULL,
                             "display_name" varchar(255)
);