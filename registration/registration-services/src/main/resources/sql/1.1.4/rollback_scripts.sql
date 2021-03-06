drop table reg.template;

CREATE TABLE reg.template ("ID" VARCHAR(36) NOT NULL, "NAME" VARCHAR(128) NOT NULL, "DESCR" VARCHAR(256), "FILE_FORMAT_CODE" VARCHAR(36) NOT NULL, "MODEL" VARCHAR(128), "FILE_TXT" VARCHAR(4086), "MODULE_ID" VARCHAR(36), "MODULE_NAME" VARCHAR(128), "TEMPLATE_TYP_CODE" VARCHAR(36) NOT NULL, "LANG_CODE" VARCHAR(3) NOT NULL, "IS_ACTIVE" BOOLEAN NOT NULL, "CR_BY" VARCHAR(32) NOT NULL, "CR_DTIMES" TIMESTAMP NOT NULL, "UPD_BY" VARCHAR(32), "UPD_DTIMES" TIMESTAMP, "IS_DELETED" BOOLEAN, "DEL_DTIMES" TIMESTAMP);

ALTER TABLE reg.template ADD CONSTRAINT "PK_TMPLT_ID" PRIMARY KEY ("ID", "LANG_CODE");