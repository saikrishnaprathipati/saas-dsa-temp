CREATE TABLE  SGAS."COMPLETE_WEB_APP_DSA"
(
  DSA_APPLICATION_ID          NUMBER(10)            NOT NULL,
  STUD_REF_NO                 NUMBER(10)                    ,
  OBJECT_ID			 		  VARCHAR2(44 BYTE) 	        ,
  SESSION_CODE                NUMBER(4)                     ,
  WEB_SUBMITTED               DATE 					        ,
  DSA_APPLICATION_TYPE        VARCHAR2(50 BYTE)     
);

ALTER TABLE SGAS."COMPLETE_WEB_APP_DSA" ADD PRIMARY KEY(DSA_APPLICATION_ID);



CREATE TABLE  SGAS."COMPLETE_WEB_APP_DSA$TEST"
(
  AUD_TIMESTAMP               DATE 					        ,
  AUD_USER                    VARCHAR2(30 BYTE)             ,
  DSA_APPLICATION_ID          NUMBER(10)            NOT NULL,
  STUD_REF_NO                 NUMBER(10)                    ,
  OBJECT_ID			 		  VARCHAR2(44 BYTE) 	        ,
  SESSION_CODE                NUMBER(4)                     ,
  WEB_SUBMITTED               DATE 					        ,
  DSA_APPLICATION_TYPE        VARCHAR2(50 BYTE)     
);


CREATE TABLE  SGAS."DSA_APPLICATION_PDF"
(
DSA_APPLICATION_ID          NUMBER(10)            NOT NULL,
PDF                         BLOB                          ,
STUD_REF_NO                 NUMBER(10)                    ,
LEARNER_ID                  VARCHAR2(10 BYTE)
);