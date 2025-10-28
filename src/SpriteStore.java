import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public final class SpriteStore {
   private static final Map<String, Image> cache = new HashMap();

   public static Image get(String var0) {
      try {
         Image var1 = (Image)cache.get(var0);
         if (var1 != null) {
            return var1;
         } else {
            BufferedImage var3 = ImageIO.read(new File(var0));
            cache.put(var0, var3);
            return var3;
         }
      } catch (Exception var2) {
         return null;
      }
   }
}