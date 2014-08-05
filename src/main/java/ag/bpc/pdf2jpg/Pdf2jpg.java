package ag.bpc.pdf2jpg;

import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.pdfview.PDFCmd;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

import javax.imageio.ImageIO;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created with IntelliJ IDEA.
 * User: stephanschneider
 * Date: 28.02.14
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
@Path("/")
public class Pdf2jpg extends ClassNamesResourceConfig {

    private static final String BASE_DIR = "";
    private static final String FORMAT = "jpg";

    public Pdf2jpg() {
        super(Pdf2jpg.class);
    }

  /*  @GET
    public String helloWorld(){
        return "PDF2JPG am Start";
    }  */

    @GET
    public String processPdfToImage(@QueryParam("filename") String filename) throws IOException {
        File pdfFile = new File(BASE_DIR + filename + ".pdf");
        RandomAccessFile raf = new RandomAccessFile(pdfFile, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        PDFFile pdf = new PDFFile(buf);
        for (int pageNumber = 1; pageNumber <= pdf.getNumPages(); pageNumber++) {
            PDFPage page = pdf.getPage(pageNumber);
            int rotation = page.getRotation();

            // create the image
            Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(),
                    (int) page.getBBox().getHeight());

            int height = rect.height;
            int width = rect.width;

            if (rect.width > rect.height) {
                int swap = rect.height;
                rect.height = rect.width;
                rect.width = swap;
            }

            float ratio = ((float) height / (float) width);

            int targetWidth = 2880;
            int targetHeight = Math.round((float) targetWidth * ratio);

            width = targetWidth;
            height = targetHeight;


            BufferedImage bufferedImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);

            Image image = page.getImage(width, height,    // width & height
                    rect,                       // clip rect
                    null,                       // null for the ImageObserver
                    true,                       // fill background with white
                    true                        // block until drawing is done
            );
            Graphics2D bufImageGraphics = bufferedImage.createGraphics();
            bufImageGraphics.drawImage(image, 0, 0, null);
            String rotationString = "H";
            if (width > height) {
                rotationString = "Q";
            }
            ImageIO.write(bufferedImage, FORMAT, new File(BASE_DIR + filename + "_" + (pageNumber < 10 ? "0" + pageNumber : pageNumber) + "_" + rotationString + "." + FORMAT));
        }
        return "PDF File " + filename + " erfolgreich konvertiert!";
    }
}
