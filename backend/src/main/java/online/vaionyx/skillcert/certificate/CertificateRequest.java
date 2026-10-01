package online.vaionyx.skillcert.certificate;

import java.time.LocalDate;

public record CertificateRequest(String recipientName, String courseName, String credentialId, LocalDate issuedDate, String verificationUrl) {}
