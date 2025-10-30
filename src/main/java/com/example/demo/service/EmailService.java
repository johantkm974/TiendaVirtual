import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import io.mailtrap.model.request.emails.Attachment;

import java.util.List;
import java.util.Base64;

public class MailtrapJavaSDKTest {

    private static final String TOKEN = "8991e0826ee07862f59a0e958ba73a0e";

    public static void main(String[] args) {

        // Configuración del cliente
        final MailtrapConfig config = new MailtrapConfig.Builder()
            .token(TOKEN)
            .build();

        final MailtrapClient client = MailtrapClientFactory.createMailtrapClient(config);

        // PDF en Base64
        byte[] pdfBytes = "Tu PDF en bytes aquí".getBytes(); // reemplaza por tu PDF real
        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        // Creación del correo
        final MailtrapMail mail = MailtrapMail.builder()
            .from(new Address("hello@demomailtrap.co", "Mailtrap Test"))
            .to(List.of(new Address("johanvasqez20@gmail.com")))
            .subject("Factura de Compra")
            .text("Adjunto tu PDF de compra")
            .html("<h1>Adjunto tu PDF de compra</h1>")
            .attachments(List.of(
                Attachment.builder()
                    .filename("factura.pdf")
                    .content(pdfBase64)
                    .type("application/pdf")
                    .build()
            ))
            .category("Integration Test")
            .build();

        // Envío
        try {
            System.out.println(client.send(mail));
            System.out.println("✅ Correo enviado correctamente con PDF adjunto!");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




