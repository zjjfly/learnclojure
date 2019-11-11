package learnclojure.ch9;

import clojure.lang.PersistentHashMap;
import learnclojure.ch9.CustomException;

/**
 * @author zjjfly
 */
public class BatchJob {

    private static void performOperation(String jobId, String priority) {
        throw new CustomException(PersistentHashMap.create("jobId", jobId,
                                                           "priority", priority), "Operation Failed");
    }

    public static void runBatchJob(int customerId) {
        try {
            performOperation("verify-billings", "critical");
        } catch (CustomException e) {
            e.addInfo("customer-id", customerId);
            e.addInfo("timestamp", System.currentTimeMillis());
            throw e;
        }
    }

}

