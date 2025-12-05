--Trigger added 13/12/2023 to support DSA WEB to StEPS data transfer validation.

CREATE OR REPLACE TRIGGER SGAS.COMPLETE_WEB_APP_DSA$TRIG
/*
   Trigger added 13/12/2023 to support DSA WEB to StEPS data transfer validation.
*/
   AFTER INSERT OR UPDATE
   ON sgas.complete_web_app_dsa
   FOR EACH ROW
BEGIN
   IF INSERTING
   THEN
      INSERT INTO sgas.complete_web_app_dsa$test (aud_timestamp,
                                                  aud_user,
                                                  DSA_APPLICATION_ID,
                                                  DSA_APPLICATION_TYPE,
                                                  OBJECT_ID,
                                                  SESSION_CODE,
                                                  STUD_REF_NO,
                                                  WEB_SUBMITTED)
           VALUES (SYSDATE,
                   USER,
                   :NEW.DSA_APPLICATION_ID,
                   :NEW.DSA_APPLICATION_TYPE,
                   :NEW.OBJECT_ID,
                   :NEW.SESSION_CODE,
                   :NEW.STUD_REF_NO,
                   :NEW.WEB_SUBMITTED);
   END IF;

   IF UPDATING
   THEN
      UPDATE sgas.complete_web_app_dsa$test
         SET OBJECT_ID = :new.OBJECT_ID
       WHERE STUD_REF_NO = :new.STUD_REF_NO;
   END IF;
END;
/