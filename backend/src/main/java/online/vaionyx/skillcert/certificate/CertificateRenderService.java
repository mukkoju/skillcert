package online.vaionyx.skillcert.certificate;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** Composes dynamic credential data on top of the locked Canva background. */
@Service
public class CertificateRenderService {
  private static final int WIDTH = 3508;
  private static final int HEIGHT = 2480;
  private static final Color NAVY = new Color(5, 27, 82);
  private static final Color SUPPORTING_NAVY = new Color(31, 63, 126);
  private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMMM uuuu");
  private static final Font PLAYFAIR_DISPLAY = loadPlayfairDisplay();

  public byte[] renderPng(CertificateRequest request) {
    try {
      BufferedImage canvas = loadMaster();
      Graphics2D graphics = canvas.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics.setColor(NAVY);

      drawCentered(graphics, request.recipientName(), 1215, 176, 2320, true);
      drawCentered(graphics, request.courseName(), 1620, 122, 2420, true);
      graphics.setColor(SUPPORTING_NAVY);
      drawFittedAt(graphics, request.credentialId(), 1260, 2348, 36, 500, false);
      drawFittedAt(graphics, DISPLAY_DATE.format(request.issuedDate()), 2050, 2348, 36, 500, false);
      drawQr(graphics, request.verificationUrl(), 2923, 1825, 250);
      graphics.setColor(SUPPORTING_NAVY);
      drawCenteredAt(graphics, "skillcert.vaionyxsolutions.online/verify/" + request.credentialId(), 3048, 2190, 24, false);
      graphics.dispose();

      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ImageIO.write(canvas, "png", output);
      return output.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to render certificate", exception);
    }
  }

  public byte[] renderPdf(CertificateRequest request) {
    byte[] png = renderPng(request);
    try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
      document.addPage(page);
      PDImageXObject image = PDImageXObject.createFromByteArray(document, png, "skillcert-certificate");
      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        content.drawImage(image, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
      }
      document.save(output);
      return output.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to create certificate PDF", exception);
    }
  }

  private BufferedImage loadMaster() throws IOException {
    try (ByteArrayInputStream stream = new ByteArrayInputStream(new ClassPathResource("certificate/skillcert-a4-static-master.png").getInputStream().readAllBytes())) {
      BufferedImage master = ImageIO.read(stream);
      if (master == null || master.getWidth() != WIDTH || master.getHeight() != HEIGHT) throw new IllegalStateException("Certificate master must be 3508 x 2480 pixels");
      BufferedImage copy = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = copy.createGraphics(); graphics.drawImage(master, 0, 0, null); graphics.dispose();
      return copy;
    }
  }

  private void drawQr(Graphics2D graphics, String data, int x, int y, int size) {
    try {
      BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, Map.of(EncodeHintType.MARGIN, 1));
      graphics.setColor(Color.WHITE); graphics.fillRect(x, y, size, size);
      graphics.setColor(Color.BLACK);
      for (int row = 0; row < matrix.getHeight(); row++) for (int column = 0; column < matrix.getWidth(); column++) if (matrix.get(column, row)) graphics.fillRect(x + column, y + row, 1, 1);
    } catch (Exception exception) { throw new IllegalStateException("Unable to create verification QR", exception); }
  }

  private void drawCentered(Graphics2D graphics, String text, int baselineY, int fontSize, int maxWidth, boolean displayFont) {
    Font font = font(displayFont, fontSize);
    while (font.getSize() > 34 && graphics.getFontMetrics(font).stringWidth(text) > maxWidth) font = font.deriveFont((float) font.getSize() - 2);
    graphics.setFont(font);
    graphics.drawString(text, (WIDTH - graphics.getFontMetrics().stringWidth(text)) / 2, baselineY);
  }

  private void drawFittedAt(Graphics2D graphics, String text, int x, int baselineY, int fontSize, int maxWidth, boolean displayFont) {
    Font font = font(displayFont, fontSize);
    while (font.getSize() > 18 && graphics.getFontMetrics(font).stringWidth(text) > maxWidth) font = font.deriveFont((float) font.getSize() - 1);
    graphics.setFont(font);
    graphics.drawString(text, x, baselineY);
  }

  private void drawCenteredAt(Graphics2D graphics, String text, int centerX, int baselineY, int fontSize, boolean displayFont) {
    graphics.setFont(font(displayFont, fontSize));
    graphics.drawString(text, centerX - graphics.getFontMetrics().stringWidth(text) / 2, baselineY);
  }

  private Font font(boolean displayFont, int size) {
    return displayFont ? PLAYFAIR_DISPLAY.deriveFont((float) size) : new Font(Font.SANS_SERIF, Font.PLAIN, size);
  }

  private static Font loadPlayfairDisplay() {
    try (InputStream stream = new ClassPathResource("fonts/PlayfairDisplay-Bold.ttf").getInputStream()) {
      return Font.createFont(Font.TRUETYPE_FONT, stream);
    } catch (Exception exception) {
      throw new IllegalStateException("Playfair Display font could not be loaded", exception);
    }
  }
}
