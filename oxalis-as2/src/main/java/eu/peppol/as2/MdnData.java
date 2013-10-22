package eu.peppol.as2;

import java.util.Date;
import java.util.Map;

/**
 * Holds the data in a Message Disposition Notification (MDN). Instances of this class must be transformed into a
 * MIME message for transmission.
 *
 * @author steinar
 *         Date: 09.10.13
 *         Time: 21:01
 */
public class MdnData {


    private final String subject;
    private final String as2From;
    private final String as2To;
    private final As2Disposition as2Disposition;
    private final String mic;
    private Date date;
    private String messageId;

    private MdnData(Builder builder) {
        this.subject = builder.subject;
        this.as2From = builder.as2From;
        this.as2To = builder.as2To;
        this.as2Disposition = builder.disposition;
        this.mic = builder.mic;
        this.date = builder.date;
        this.messageId = builder.messageId;

    }

    public String getSubject() {
        return subject;
    }

    public String getAs2From() {
        return as2From;
    }

    public String getAs2To() {
        return as2To;
    }

    public As2Disposition getAs2Disposition() {
        return as2Disposition;
    }

    public String getMic() {
        return mic;
    }

    public Date getDate() {
        return date;
    }

    public String getMessageId() {
        return messageId;
    }

    public static class Builder {
        String subject;
        String as2From;
        String as2To;

        As2Disposition disposition;

        String mic;
        Date date;
        String messageId;

        public Builder date(Date date){
            this.date = date;
            return this;
        }

        Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        Builder as2From(String as2From) {
            this.as2From = as2From;
            return this;
        }

        Builder as2To(String as2To) {
            this.as2To = as2To;
            return this;
        }

        Builder disposition(As2Disposition disposition) {
            this.disposition = disposition;
            return this;
        }

        Builder mic(String mic) {
            this.mic = mic;
            return this;
        }

        Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        MdnData build() {
            required(as2From, "as2From");
            required(as2To, "as2To");
            required(disposition, "disposition");

            return new MdnData(this);
        }


        private void required(Object object, String name) {
            if (object == null) {
                throw new IllegalStateException("Required property '" + name + "' not set.");
            }
        }

        public static MdnData buildProcessedOK(Map<String,String> headers) {
            Builder builder = new Builder();
            builder.subject("Your requested MDN response - processed OK")
                    .disposition(As2Disposition.processed());
            addStandardHeaders(headers, builder);

            return new MdnData(builder);
        }


        public static MdnData buildFailureFromHeaders(Map<String, String> map, String msg) {
            Builder builder = new Builder();

            builder.subject("Your requested MDN response - failed")
                    .disposition(As2Disposition.failed(msg));

            addStandardHeaders(map, builder);

            return new MdnData(builder);
        }

        public static MdnData buildProcessingErrorFromHeaders(Map<String, String> headers, String msg) {
            Builder builder = new Builder();

            builder.subject("Your requested MDN response - processing error")
                    .disposition(As2Disposition.processedWithError(msg));

            addStandardHeaders(headers, builder);

            return new MdnData(builder);

        }

        private static void addStandardHeaders(Map<String, String> headers, Builder builder) {
            builder.as2From(headers.get(As2Header.AS2_TO.getHttpHeaderName()))
                    .as2To(headers.get(As2Header.AS2_FROM.getHttpHeaderName()))
                    .date(new Date())
                    .messageId(headers.get(As2Header.MESSAGE_ID.getHttpHeaderName()));
        }
    }
}