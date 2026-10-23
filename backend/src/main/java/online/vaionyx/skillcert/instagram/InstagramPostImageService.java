package online.vaionyx.skillcert.instagram;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import online.vaionyx.skillcert.certificate.CertificateStorageService;
import org.springframework.stereotype.Service;

@Service
class InstagramPostImageService {
  private final CertificateStorageService storage;

  InstagramPostImageService(CertificateStorageService storage) { this.storage = storage; }

  String createAndStore(LocalDate date, InstagramPublishingService.PostTheme theme) {
    String objectKey = "instagram/daily/skillcert-" + date + ".jpg";
    return storage.storePublicImage(objectKey, render(theme), "image/jpeg");
  }

  private byte[] render(InstagramPublishingService.PostTheme theme) {
    try {
      BufferedImage image = new BufferedImage(1080, 1080, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = image.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setPaint(new GradientPaint(0, 0, new Color(3, 17, 39), 1080, 1080, new Color(8, 47, 91)));
      g.fillRect(0, 0, 1080, 1080);

      g.setColor(new Color(19, 202, 246, 30));
      g.fillOval(660, -200, 650, 650);
      g.fillOval(-260, 720, 600, 600);

      g.setColor(Color.WHITE);
      g.setFont(new Font("SansSerif", Font.BOLD, 58));
      g.drawString("Skill", 84, 118);
      g.setColor(new Color(24, 192, 247));
      g.drawString("Cert", 228, 118);
      g.setColor(new Color(194, 216, 242));
      g.setFont(new Font("SansSerif", Font.BOLD, 17));
      g.drawString("BY VAIONYX", 88, 150);

      g.setColor(new Color(250, 201, 89));
      g.setFont(new Font("Serif", Font.BOLD, 88));
      drawCentered(g, theme.headline(), 340);
      g.setColor(Color.WHITE);
      g.setFont(new Font("Serif", Font.BOLD, 80));
      drawCentered(g, theme.subheadline(), 435);

      RoundRectangle2D card = new RoundRectangle2D.Float(96, 535, 888, 230, 34, 34);
      g.setColor(new Color(3, 13, 31, 220));
      g.fill(card);
      g.setColor(new Color(35, 205, 242));
      g.draw(card);
      g.setFont(new Font("SansSerif", Font.PLAIN, 36));
      g.setColor(new Color(211, 228, 247));
      drawCentered(g, theme.detail(), 635);
      g.setFont(new Font("SansSerif", Font.BOLD, 40));
      g.setColor(new Color(169, 226, 83));
      drawCentered(g, "TAKE THE 5-MIN ASSESSMENT", 710);

      RoundRectangle2D cta = new RoundRectangle2D.Float(250, 846, 580, 96, 48, 48);
      g.setColor(new Color(145, 216, 58));
      g.fill(cta);
      g.setColor(new Color(13, 32, 8));
      g.setFont(new Font("SansSerif", Font.BOLD, 34));
      drawCentered(g, "skillcert.vaionyxsolutions.online", 907);
      g.dispose();

      ByteArrayOutputStream output = new ByteArrayOutputStream();
      if (!ImageIO.write(image, "jpg", output)) throw new IllegalStateException("JPEG writer is unavailable");
      return output.toByteArray();
    } catch (Exception exception) {
      throw new IllegalStateException("Daily Instagram image could not be generated", exception);
    }
  }

  private static void drawCentered(Graphics2D g, String value, int y) {
    int width = g.getFontMetrics().stringWidth(value);
    g.drawString(value, (1080 - width) / 2, y);
  }
}
