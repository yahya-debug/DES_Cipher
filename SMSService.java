import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {

    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String FROM_NUMBER = System.getenv("TWILIO_FROM_NUMBER");

    public static String sendSMS(String phoneNumber, String messageBody) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(FROM_NUMBER),
                messageBody
        ).create();
        return message.getSid();
    }
}
