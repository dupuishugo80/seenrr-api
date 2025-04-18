package com.seenrr.seenrr.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.resource.Emailv31;

@Service
@PropertySource("classpath:application.properties")
public class MailjetEmailService {
    @Autowired
    private Environment env;

    public void sendEmail(String fromEmail, String toEmail, String toName, String subject, String textContent, String htmlContent) throws Exception {
        String apiKey = env.getProperty("mailjet.api-key");
        String apiSecretKey = env.getProperty("mailjet.api-secret");

        ClientOptions options = ClientOptions.builder()
            .apiKey(apiKey)
            .apiSecretKey(apiSecretKey)
            .build();

        MailjetClient client = new MailjetClient(options);

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
            .property(Emailv31.MESSAGES, new JSONArray()
                .put(new JSONObject()
                    .put(Emailv31.Message.FROM, new JSONObject()
                        .put("Email", fromEmail)
                        .put("Name", "Seenrr"))
                    .put(Emailv31.Message.TO, new JSONArray()
                        .put(new JSONObject()
                            .put("Email", toEmail)
                            .put("Name", toName)))
                    .put(Emailv31.Message.SUBJECT, subject)
                    .put(Emailv31.Message.TEXTPART, textContent)
                    .put(Emailv31.Message.HTMLPART, htmlContent)));
        client.post(request);
    }
}
