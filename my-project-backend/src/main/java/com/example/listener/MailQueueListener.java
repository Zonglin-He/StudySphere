package com.example.listener;

import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    /** Main entry: choose plain text or HTML based on payload flags */
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        try {
            String email      = str(data.get("email"));                 // required
            String type       = str(data.get("type"));                  // register/reset/modify (optional)
            Integer code      = toInt(data.get("code"));                // optional
            String title      = str(data.get("title"));                 // optional (override)
            String content    = str(data.get("content"));               // optional (override)
            String fromName   = def(str(data.get("fromName")), "My Project");

            boolean htmlFlag  = bool(data.get("html"));                 // force HTML
            String inlineCp   = str(data.get("inlineClasspath"));       // e.g. "mail/logo.jpg"
            String imageUrl   = str(data.get("imageUrl"));              // e.g. "https://.../banner.png"
            String attachPath = str(data.get("attachmentPath"));        // e.g. "/tmp/report.pdf"

            // Default English subject & content if not provided
            if (isBlank(title) || isBlank(content)) {
                SubjectContent sc = defaultsByType(type, code);
                if (isBlank(title))   title = sc.subject;
                if (isBlank(content)) content = sc.text;
            }

            // Send HTML if explicitly requested or any rich-media provided; otherwise send plain text
            if (htmlFlag || !isBlank(inlineCp) || !isBlank(imageUrl) || !isBlank(attachPath)) {
                sendHtml(email, title, content, code, fromName, inlineCp, imageUrl, attachPath);
            } else {
                sendPlain(email, title, content);
            }
        } catch (Exception e) {
            e.printStackTrace(); // replace with logger in production
        }
    }

    /** Fallback: drop non-JSON/legacy messages to prevent consumer from being stuck */
    @RabbitHandler
    public void receiveBytes(byte[] body) {
        System.out.println("Drop non-Map message, length=" + body.length);
    }

    /* ==================== Plain text ==================== */

    private void sendPlain(String email, String title, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(email);
        message.setSubject(title);
        message.setText(content);
        sender.send(message);
    }

    /* ==================== HTML Rich mail ==================== */

    private void sendHtml(String email,
                          String title,
                          String contentText,
                          Integer code,
                          String fromName,
                          String inlineClasspath,
                          String imageUrl,
                          String attachmentPath) throws Exception {

        var mime = sender.createMimeMessage();
        var helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());

        helper.setFrom(new InternetAddress(username, fromName, StandardCharsets.UTF_8.name()));
        helper.setTo(email);
        helper.setSubject(title);

        String codeBlock = (code != null)
                ? "<p style=\"font-size:14px;margin:8px 0 0;\">Your code: " +
                "<b style=\"font-size:18px;color:#2563eb;\">" + code + "</b></p>"
                : "";

        String imagePlaceholder =
                (!isBlank(inlineClasspath) || !isBlank(imageUrl))
                        ? "<img src='cid:banner' style='display:block;width:100%;max-width:560px;height:auto;border:0;margin-top:12px;'/>"
                        : "";

        String html = """
                <div style="font-family:Arial,sans-serif;line-height:1.6;max-width:600px;margin:0 auto;">
                  <h2 style="margin:0 0 12px;">%s</h2>
                  <div style="font-size:14px;margin:0 0 12px;white-space:pre-wrap;">%s</div>
                  %s
                  <p style="font-size:12px;color:#666;margin-top:12px;">
                    If you didn't request this, you can safely ignore this email.
                  </p>
                  %s
                </div>
                """.formatted(escape(title), escape(contentText), codeBlock, imagePlaceholder);

        helper.setText(html, true);

        // Inline classpath image OR remote image (use the first available)
        boolean bannerSet = false;
        if (!isBlank(inlineClasspath)) {
            ClassPathResource res = new ClassPathResource(inlineClasspath);
            if (res.exists()) {
                helper.addInline("banner", res);
                bannerSet = true;
            }
        }
        if (!bannerSet && !isBlank(imageUrl)) {
            byte[] bytes = new URL(imageUrl).openStream().readAllBytes();
            String ct = URLConnection.guessContentTypeFromName(imageUrl);
            helper.addInline("banner", new ByteArrayResource(bytes), def(ct, "image/jpeg"));
        }

        // Optional attachment
        if (!isBlank(attachmentPath)) {
            FileSystemResource file = new FileSystemResource(attachmentPath);
            if (file.exists()) {
                helper.addAttachment(Objects.requireNonNullElse(file.getFilename(), "attachment"), file);
            }
        }

        sender.send(mime);
    }

    /* ==================== Defaults (English) ==================== */

    private static class SubjectContent {
        final String subject;
        final String text;
        SubjectContent(String s, String t) { this.subject = s; this.text = t; }
    }

    private SubjectContent defaultsByType(String type, Integer code) {
        String c = (code == null) ? "—" : String.valueOf(code);
        return switch (type == null ? "" : type) {
            case "register" -> new SubjectContent(
                    "Verify your email",
                    "Your verification code is: " + c +
                            ". It will expire in 3 minutes. For your account’s security, do not share this code with anyone."
            );
            case "reset" -> new SubjectContent(
                    "Password reset code",
                    "You requested a password reset. Your verification code is: " + c +
                            ". It will expire in 3 minutes. If you didn’t request this, you can safely ignore this email."
            );
            case "modify" -> new SubjectContent(
                    "Verify your new email",
                    "You’re trying to bind a new email to your account. Your verification code is: " + c +
                            ". It will expire in 3 minutes. If this wasn’t you, please ignore this email."
            );
            default -> new SubjectContent(
                    "Notification",
                    "This is a system notification email" + (code != null ? ". Verification code: " + c : ".")
            );
        };
    }

    /* ==================== Utils ==================== */

    private static String str(Object o) { return o == null ? null : String.valueOf(o).trim(); }
    private static String def(String v, String d) { return isBlank(v) ? d : v; }
    private static boolean bool(Object o) { return o != null && Boolean.parseBoolean(String.valueOf(o)); }
    private static Integer toInt(Object o) {
        try { return (o == null) ? null : Integer.parseInt(String.valueOf(o).trim()); }
        catch (Exception e) { return null; }
    }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
