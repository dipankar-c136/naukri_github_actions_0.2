package com.automation.utils;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailUtils {

    public static String getOtp(String email, String appPassword) throws Exception {
        System.out.println("[GmailUtils] Connecting to Gmail...");
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");

        // Connect
        store.connect("imap.gmail.com", email, appPassword);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        long startTime = System.currentTimeMillis();
        // Poll for 60 seconds
        while (System.currentTimeMillis() - startTime < 60000) {
            int messageCount = inbox.getMessageCount();
            // Check only the last 5 emails to be efficient
            int start = Math.max(1, messageCount - 5);
            Message[] messages = inbox.getMessages(start, messageCount);

            for (int i = messages.length - 1; i >= 0; i--) {
                Message msg = messages[i];
                // Match Subject and Time (must be recent < 3 mins)
                if (msg.getSubject() != null && msg.getSubject().contains("Your OTP for logging in Naukri account")) {
                    if (System.currentTimeMillis() - msg.getReceivedDate().getTime() < 180000) {
                        System.out.println("[GmailUtils] OTP Email Found: " + msg.getSubject());
                        String content = getTextFromMessage(msg);
                        // Regex to find 6-digit number
                        Matcher m = Pattern.compile("\\b\\d{6}\\b").matcher(content);
                        if (m.find()) {
                            String otp = m.group(0);
                            inbox.close(false);
                            store.close();
                            return otp;
                        }
                    }
                }
            }
            try { Thread.sleep(5000); } catch (InterruptedException e) { break; } // Wait 5s before next check
        }
        inbox.close(false);
        store.close();
        throw new RuntimeException("OTP Email not found within 60s timeout.");
    }

    private static String getTextFromMessage(Part p) throws Exception {
        if (p.isMimeType("text/plain")) return (String) p.getContent();
        if (p.isMimeType("text/html")) return ((String) p.getContent()).replaceAll("<[^>]*>", " ");
        if (p.isMimeType("multipart/*")) {
            MimeMultipart mp = (MimeMultipart) p.getContent();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) result.append(getTextFromMessage(mp.getBodyPart(i)));
            return result.toString();
        }
        return "";
    }
}