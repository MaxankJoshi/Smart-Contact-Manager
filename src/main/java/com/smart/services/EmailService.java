package com.smart.services;

import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

@Service
public class EmailService {
	public boolean sendEmail(String subject,String message,String to) {
//      Rest of the code...

      boolean f = false;

      String from = "";

//      variable for gmail
      String host="smtp.gmail.com";

//      get the system properties
      Properties properties = System.getProperties();
      System.out.println("PROPERTIES"+properties);

//      Setting important information to properties object

//      host set
      properties.put("mail.smtp.host",host);
      properties.put("mail.smtp.port","465");
      properties.put("mail.smtp.ssl.enable","true");
      properties.put("mail.smtp.auth","true");

//      Step 1: To get the session object
      Session session = Session.getInstance(properties, new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication("mayankjoshi233@gmail.com","oiubmjxhbojzkqea");
          }
      });

      session.setDebug(true);

//      Step 2 : Compose the message [text,multi media]
      MimeMessage m = new MimeMessage(session);

      try{
//          From email
          m.setFrom(from);

//          Adding recipient to message
          m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

//          Adding subject to message
          m.setSubject(subject);

//          Adding text to message
//          m.setText(message);
          m.setContent(message,"text/html");

//          Send

//          Step 3: Send the message using Transport class
          Transport.send(m);

          System.out.println("Sent successfully...");

          f = true;
      }

      catch(Exception e) {
          e.printStackTrace();
      }

      return f;
  }
}
