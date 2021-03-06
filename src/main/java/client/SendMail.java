package client;



import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
public class SendMail {

    public void sendEmail(String to, String msg)
    {
        final String username = "noflow.noreply@gmail.com";
        final String password = "Noflow123";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject("Registration");
            message.setText(msg);

            Transport.send(message);

            System.out.println("SendMail : SENDING CONFIRMATION CODE TO USER - SUCCESS");

        }

        catch (MessagingException e)
        {
            System.out.println("SendMail : ERROR SENDING CONFIRMATION CODE");
        }
    }
}
