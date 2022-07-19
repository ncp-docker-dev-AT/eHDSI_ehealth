package eu.europa.ec.sante.ehdsi.openncp.abusedetection;

public enum AbuseTransactionType {
        TRANSACTION_UNKNOWN("?"),
        XCPD_SERVICE_REQUEST("XCPD_SERVICE_REQUEST"),
        XCA_SERVICE_REQUEST("XCA_SERVICE_REQUEST"),
        XDR_SERVICE_REQUEST("XDR_SERVICE_REQUEST");

        private String transaction;
        AbuseTransactionType(String transaction) {
            this.transaction = transaction;
        }

        public String getTransaction() {
            return transaction;
        }

        @Override
        public String toString() {
            return "transactions{" +
                    "transaction='" + transaction + '\'' +
                    '}';
        }

}
