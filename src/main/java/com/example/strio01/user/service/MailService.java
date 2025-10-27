package com.example.strio01.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetMail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("jaeminson153@gmail.com"); //
            helper.setTo(to);
            helper.setSubject("[Lung-Mate] 비밀번호 재설정 링크 안내");
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;line-height:1.6;'>"
                    + "<h2>비밀번호 재설정 안내</h2>"
                    + "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>"
                    + "<a href='" + resetLink + "' "
                    + "style='display:inline-block;padding:10px 20px;background:#007bff;color:#fff;"
                    + "text-decoration:none;border-radius:5px;'>비밀번호 재설정</a>"
                    + "<p>만약 요청하지 않으셨다면 이 메일은 무시하셔도 됩니다.</p>"
                    + "</div>",
                    true);

            mailSender.send(message);
            log.info("비밀번호 재설정 메일 전송 완료: {}", to);

        } catch (MessagingException e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("메일 전송 중 오류가 발생했습니다.");
        }
    }
}
