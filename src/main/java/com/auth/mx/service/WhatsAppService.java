package com.auth.mx.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    // Estas credenciales pueden venir de application.properties
    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.fromWhatsAppNumber}")
    private String fromWhatsAppNumber;

    // Inicializa Twilio
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendWhatsAppMessage(String to, String body) {
        // Asegúrate de inicializar Twilio al iniciar el servicio o en el constructor
        init();
        Message.creator(
                new PhoneNumber("whatsapp:" + to),
                new PhoneNumber("whatsapp:" + fromWhatsAppNumber),
                body
        ).create();
    }
}
