package com.example.demo.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.return.url}")
    private String returnUrl;

    @Value("${paypal.cancel.url}")
    private String cancelUrl;

    private APIContext getAPIContext() {
        return new APIContext(clientId, clientSecret, mode);
    }

    /**
     * Crea un pago en PayPal con conversión automática de Soles (PEN) a Dólares (USD)
     */
    public Payment createPayment(Double totalPEN, String currency, String description) throws PayPalRESTException {
        // 🔹 TIPO DE CAMBIO (puedes actualizarlo manualmente o hacerlo dinámico)
        double tipoCambio = 3.80; // 1 USD = 3.80 PEN (actualízalo si cambia)

        // 🔹 Convertir a dólares
        double totalUSD = totalPEN / tipoCambio;
        String totalFormateado = String.format("%.2f", totalUSD);

        // 🔹 Log informativo
        System.out.println("💱 Conversión de moneda:");
        System.out.println("   Monto original (S/): " + String.format("%.2f", totalPEN));
        System.out.println("   Monto convertido (USD): $" + totalFormateado);

        // 🔹 Crear monto en dólares para PayPal
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(totalFormateado);

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(returnUrl);
        payment.setRedirectUrls(redirectUrls);

        // 🔹 Crear pago
        return payment.create(getAPIContext());
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);

        return payment.execute(getAPIContext(), paymentExecute);
    }
}