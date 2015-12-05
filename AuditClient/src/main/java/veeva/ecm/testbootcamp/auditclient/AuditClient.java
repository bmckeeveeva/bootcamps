package veeva.ecm.testbootcamp.auditclient;


public interface AuditClient {
    /**
     * Audits any file-write action that occurs
     *
     * @param userId id of the user that initiated the file-writing
     * @param fileName name of the file that was written
     */
    void auditFileWriteAction(String userId, String fileName);
}
