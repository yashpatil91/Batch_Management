package com.batchmanagement.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 🔥 HTML EMAIL METHOD
    public void sendBatchEmail(String to, String name, String domain, String startDate) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("New Batch Created");

            // 🔥 HTML TEMPLATE
            String htmlContent =
                    "<div style='font-family: Arial; padding:20px;'>"
                            + "<h2 style='color:#0d7377;'>New Batch Assigned</h2>"
                            + "<p>Hello <b>" + name + "</b>,</p>"
                            + "<p>A new batch has been created for you:</p>"
                            + "<div style='background:#f3f4f6; padding:15px; border-radius:8px;'>"
                            + "<p><b>Domain:</b> " + domain + "</p>"
                            + "<p><b>Start Date:</b> " + startDate + "</p>"
                            + "</div>"
                            + "<br>"
                            + "<a href='http://localhost:8080/login.html' "
                            + "style='background:#0d7377; color:white; padding:10px 20px; text-decoration:none; border-radius:5px;'>"
                            + "Login Now</a>"
                            + "<br><br>"
                            + "<p>Regards,<br><b>Admin</b></p>"
                            + "</div>";

            // 🔥 VERY IMPORTANT (true = HTML)
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // false = plain text
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}