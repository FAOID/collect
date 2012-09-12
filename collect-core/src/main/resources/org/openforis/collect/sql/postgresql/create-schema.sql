CREATE SCHEMA "desre";
CREATE SEQUENCE "desre"."ofc_record_id_seq";
CREATE SEQUENCE "desre"."ofc_survey_id_seq";
CREATE SEQUENCE "desre"."ofc_taxonomy_id_seq";
CREATE SEQUENCE "desre"."ofc_taxon_id_seq";
CREATE SEQUENCE "desre"."ofc_taxon_vernacular_name_id_seq";
CREATE SEQUENCE "desre"."ofc_user_id_seq"	;
CREATE SEQUENCE "desre"."ofc_user_role_id_seq";

----------------------------
--- BEGIN GENERATED CODE ---
----------------------------
CREATE TABLE "desre"."ofc_application_info"  ( 
	"version"	varchar(25) NOT NULL 
);
CREATE TABLE "desre"."ofc_config"  ( 
	"name" 	varchar(25) NOT NULL,
	"value"	varchar(255) NOT NULL,
	PRIMARY KEY("name")
);
CREATE TABLE "desre"."ofc_logo"  ( 
	"pos"  	integer NOT NULL,
	"image"	bytea NOT NULL,
	PRIMARY KEY("pos")
);
CREATE TABLE "desre"."ofc_record"  ( 
	"id"                       	integer NOT NULL,
	"survey_id"					integer NOT NULL,
	"root_entity_definition_id"	integer NOT NULL,
	"date_created"             	timestamp NULL,
	"created_by_id"            	integer NULL,
	"date_modified"            	timestamp NULL,
	"modified_by_id"           	integer NULL,
	"model_version"            	varchar(255) NOT NULL,
	"step"                     	integer NULL,
	"state"                    	char(1) NULL,
	"skipped"                  	integer NULL,
	"missing"                  	integer NULL,
	"errors"                   	integer NULL,
	"warnings"                 	integer NULL,
	"key1"                     	varchar(2048) NULL,
	"key2"                     	varchar(2048) NULL,
	"key3"                     	varchar(2048) NULL,
    "key4"                     	varchar(2048) NULL,
    "key5"                     	varchar(2048) NULL,
	"count1"                   	integer NULL,
	"count2"                   	integer NULL,
	"count3"                   	integer NULL,
	"count4"                   	integer NULL,
	"count5"                   	integer NULL,
	"data1"                    	bytea NULL,
	"data2"                    	bytea NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "desre"."ofc_survey"  ( 
	"id"  	integer NOT NULL,
	"name"	varchar(255) NOT NULL,
	"uri" 	varchar(255) NOT NULL,
	"idml"	text NOT NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "desre"."ofc_taxon"  ( 
	"id"             	integer NOT NULL,
	"taxon_id"       	integer NOT NULL,
	"code"           	varchar(32) NOT NULL,
	"scientific_name"	varchar(255) NOT NULL,
	"taxon_rank"     	varchar(128) NOT NULL,
	"taxonomy_id"    	integer NOT NULL,
	"step"           	integer NOT NULL,
	"parent_id"      	integer NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "desre"."ofc_taxon_vernacular_name"  ( 
	"id"              	integer NOT NULL,
	"vernacular_name" 	varchar(255) NULL,
	"language_code"   	varchar(3) NOT NULL,
	"language_variety"	varchar(255) NULL,
	"taxon_id"        	integer NULL,
	"step"            	integer NOT NULL,
	"qualifier1"	varchar(255) NULL,
    "qualifier2"	varchar(255) NULL,
    "qualifier3"	varchar(255) NULL,
	PRIMARY KEY("id")
);
COMMENT ON COLUMN "desre"."ofc_taxon_vernacular_name"."language_variety" IS 'Dialect, lect, sublanguage or other';
CREATE TABLE "desre"."ofc_taxonomy"  ( 
	"id"      	integer NOT NULL,
	"name"    	varchar(255) NOT NULL,
	"metadata"	text NOT NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "desre"."ofc_user"  ( 
	"id"      	integer NOT NULL,
	"username"	varchar(255) NOT NULL,
	"password"	varchar(255) NOT NULL,
	"enabled" 	char(1) NOT NULL DEFAULT 'Y',
	PRIMARY KEY("id")
);
CREATE TABLE "desre"."ofc_user_role"  ( 
	"id"     	integer NOT NULL,
	"user_id"	integer NOT NULL,
	"role"   	varchar(256) NULL,
	PRIMARY KEY("id")
);
ALTER TABLE "desre"."ofc_survey"
	ADD CONSTRAINT "ofc_survey_name_key"
	UNIQUE ("name");
ALTER TABLE "desre"."ofc_survey"
	ADD CONSTRAINT "ofc_survey_uri_key"
	UNIQUE ("uri");
ALTER TABLE "desre"."ofc_taxon"
	ADD CONSTRAINT "ofc_taxon_id_key"
	UNIQUE ("taxon_id", "taxonomy_id");
ALTER TABLE "desre"."ofc_taxonomy"
	ADD CONSTRAINT "ofc_taxonomy_name_key"
	UNIQUE ("name");
ALTER TABLE "desre"."ofc_taxon_vernacular_name"
	ADD CONSTRAINT "ofc_taxon_vernacular_name_taxon_fkey"
	FOREIGN KEY("taxon_id")
	REFERENCES "desre"."ofc_taxon"("id");
ALTER TABLE "desre"."ofc_taxon"
	ADD CONSTRAINT "ofc_taxon_parent_fkey"
	FOREIGN KEY("parent_id")
	REFERENCES "desre"."ofc_taxon"("id");
ALTER TABLE "desre"."ofc_taxon"
	ADD CONSTRAINT "ofc_taxon_taxonomy_fkey"
	FOREIGN KEY("taxonomy_id")
	REFERENCES "desre"."ofc_taxonomy"("id");
ALTER TABLE "desre"."ofc_user_role"
	ADD CONSTRAINT "ofc_user_user_role_fkey"
	FOREIGN KEY("user_id")
	REFERENCES "desre"."ofc_user"("id");
ALTER TABLE "desre"."ofc_record"
    ADD CONSTRAINT "ofc_record_survey_fkey"
	FOREIGN KEY("survey_id")
	REFERENCES "desre"."ofc_survey"("id");
ALTER TABLE "desre"."ofc_record"
	ADD CONSTRAINT "ofc_record_created_by_user_fkey"
	FOREIGN KEY("created_by_id")
	REFERENCES "desre"."ofc_user"("id");
ALTER TABLE "desre"."ofc_record"
	ADD CONSTRAINT "ofc_record_modified_by_user_fkey"
	FOREIGN KEY("modified_by_id")
	REFERENCES "desre"."ofc_user"("id");
--------------------------
--- END GENERATED CODE ---
--------------------------

----------------------------
--- BEGIN DEFAULT VALUES ---
----------------------------
INSERT INTO "desre"."ofc_config" ("name", "value") VALUES ('upload_path', '/home/openforis/desre-upload');
--------------------------
--- END DEFAULT VALUES ---
--------------------------
