import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import mediautil.gen.Rational;
import mediautil.gen.directio.SplitInputStream;
import mediautil.image.ImageResources;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.IFD;
import mediautil.image.jpeg.LLJTran;

public class Experiment
{
	public static void main(String[] args)
	{
		ImageMetaDataUtils.addEXIF("6666.jpg", "666.jpg");
	}
	
	public static void experiment() throws Exception
	{
		String pi = "6666.jpg";
		String po = "666.jpg";

		InputStream fip = new FileInputStream(pi); // No need to buffer
		SplitInputStream sip = new SplitInputStream(fip);
		InputStream subIp = sip.createSubStream();
		LLJTran llj = new LLJTran(subIp);

		llj.initRead(LLJTran.READ_HEADER, true, true);
		sip.attachSubReader(llj, subIp);

		// LLJTran reads the image when the below API reads from sip via
		// nextRead() calls made by sip.

		byte newThumbnail[] = getThumbnailImage(sip);
		sip.wrapup();
		fip.close();

		// Check llj for errors
		String msg = llj.getErrorMsg();
		if (msg != null) {
			System.out.println("Error in LLJTran While Loading Image: " + msg);
			Exception e = llj.getException();
			if (e != null) {
				System.out.println("Got an Exception, throwing it..");
				throw e;
			}
			System.exit(1);
		}

		// *** get exif
		AbstractImageInfo<?> imageInfo = llj.getImageInfo();
		if (!(imageInfo instanceof Exif)) {
			System.out.println("Adding a Dummy Exif Header");
			llj.addAppx(LLJTran.dummyExifHeader, 0,
					LLJTran.dummyExifHeader.length, true);
			imageInfo = llj.getImageInfo(); // This would have changed
		}

		Exif exif = (Exif) imageInfo;

		// experiment
		for (int i = 0; i < 100000000; i++) 
		{
			Entry entry = exif.getTagValue(i, true);
			if (entry == null)
			{
				continue;
			}

			System.out.print("Tag Num:" + i + ">");
			
			System.out.print("{" + entry.getValue(0) + "}");
			
			Object[] os = entry.getValues();			
			if (os != null) 
			{
				// entry.setValue(0, "1999:08:18 11:15:00");


				for (Object b : os) 
				{
					String s = b.toString();
					System.out.print(s + " ");
				}
			}
			System.out.println();
		}

		System.out.println("-----------------------------");
		System.out.println(Exif.ARTIST);
		System.out.println(Exif.COPYRIGHT);
		System.out.println(Exif.DATETIME);
		System.out.println(Exif.DATETIMEORIGINAL);
		System.out.println(Exif.DATETIMEDIGITIZED);
		
		
		// ========== test code 1
		
        IFD mainIfd = exif.getIFDs()[0];
        IFD gpsIfd = mainIfd.getIFD(Exif.GPSINFO);

        if(gpsIfd == null)
        {
            System.out.println("Gps IFD not found adding..");
            gpsIfd = new IFD(Exif.GPSINFO, Exif.LONG);
            mainIfd.addIFD(gpsIfd);
        }

        /* Set some values directly to gps IFD */
        Entry e;

        // Set Latitude
//        e = new Entry(Exif.ASCII);
//        e.setValue(0, 'N');
//        gpsIfd.setEntry(new Integer(0x0001), 0, e);
//        
//        e = new Entry(Exif.RATIONAL);
//        e.setValue(0, new Rational(45, 1));
//        e.setValue(1, new Rational(35, 1));
//        e.setValue(2, new Rational(25, 1));
//        gpsIfd.setEntry(new Integer(0x0002), 0, e);        
//        e = new Entry(Exif.BYTE);

        // Set Longitude
//        e = new Entry(Exif.ASCII);
//        e.setValue(0, 'E');
//        gpsIfd.setEntry(new Integer(0x0003), 0, e);
//        
//        e = new Entry(Exif.RATIONAL);
//        e.setValue(0, new Rational(87, 1));
//        e.setValue(1, new Rational(40, 1));
//        e.setValue(2, new Rational(30, 1));
//        gpsIfd.setEntry(new Integer(0x0004), 0, e);
        
        e = new Entry(Exif.BYTE);
        e.setValue(0, new Integer(1)); // This picture is taken underwater :-)
                                       // Use 0 if it is taken above sea
                                       // level
        gpsIfd.setEntry(new Integer(0x0005), 0, e);
        
        e = new Entry(Exif.RATIONAL);
        e.setValue(0, new Rational(666, 1));
        gpsIfd.setEntry(new Integer(0x0006), 0, e);
		
        // ====== test code 2
//        mainIfd = exif.getIFDs()[0];
//        gpsIfd = mainIfd.getIFD(Exif.COPYRIGHT);
//
//        if(gpsIfd == null)
//        {
//            System.out.println("22222 get copyright");
//            gpsIfd = new IFD(Exif.COPYRIGHT, Exif.ASCII);
//            mainIfd.addIFD(gpsIfd);
//        }
        
        e = new Entry(Exif.ASCII);
        e.setValue(0, "test 66666");
        //gpsIfd.setEntry(Exif.COPYRIGHT, 0, e);
        mainIfd.setEntry(Exif.COPYRIGHT, 0, e);
        

        e = new Entry(Exif.ASCII);
        e.setValue(0, "test 66666666");
        //gpsIfd.setEntry(Exif.COPYRIGHT, 0, e);
        mainIfd.setEntry(Exif.ARTIST, 0, e);
		
		// ========== test code 3
		Entry entry;
		 
		entry = new Entry(Exif.ASCII, "ASD");
		entry.setValue(-1, "QQQQQQ");
		exif.setTagValue(Exif.MAKE, -1, entry, true);
		
		entry = exif.getTagValue(Exif.MAKE, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, "aaaaaaaaaaaaaaaa");
		}
		
		entry = exif.getTagValue(Exif.MODEL, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, "ccccccccccccccccc");
		}
		
		// change
		String s = "9998:08:18 11:15:00";
		 
		entry = exif.getTagValue(Exif.DATETIME, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, s);
		}
		entry = exif.getTagValue(Exif.DATETIMEORIGINAL, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, s);
		}
		entry = exif.getTagValue(Exif.DATETIMEDIGITIZED, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, s);
		}
		
		
//		for (int tag = 33432 - 100; tag < 33432 + 100; tag++) 
//		{			
//			entry = new Entry(Exif.ASCII, "ASD");
//			entry.setValue(0, "QQQQQQ");
//			exif.setTagValue(tag, -1, entry, true);
//		}
		
		entry = new Entry("ASD");
		entry.setValue(0, "ASDASD");
		exif.setTagValue(Exif.ARTIST, -1, entry, true);
		
		entry = new Entry(Exif.ASCII);
		entry.setValue(0, "ASDASD");
		exif.setTagValue(Exif.ARTIST, -1, entry, true);
		
		entry = new Entry("ASD");
		entry.setValue(0, "ASDASD");
		exif.setTagValue(Exif.COPYRIGHT, -1, entry, true);
		
		entry = new Entry(Exif.ASCII);
		entry.setValue(0, "ASDASD");
		exif.setTagValue(Exif.COPYRIGHT, -1, entry, true);

		// write info
		if (imageInfo.getThumbnailLength() > 0) {
			llj.refreshAppx();
			System.out.println("Image already has a Thumbnail. Exitting..");
		} else {
			if (llj.setThumbnail(newThumbnail, 0, newThumbnail.length, ImageResources.EXT_JPG)) {
				System.out.println("Successfully Set New Thumbnail");
			} else {
				System.out.println("Error Setting New Thumbnail");
			}
		}

		// 5. Transfer the image from inputFile to outputFile replacing the new
		// Exif with the Thumbnail so that outputFile has a Thumbnail.

		fip = new BufferedInputStream(new FileInputStream(pi));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(po));

		// Replace the new Exif Header in llj while copying the image from fip
		// to out

		llj.xferInfo(fip, out, LLJTran.REPLACE, LLJTran.RETAIN);

		fip.close();
		out.close();

		// Cleanup
		llj.freeMemory();
	}

	private static byte[] getThumbnailImage(InputStream ip) throws IOException {
		ImageReader reader;
		ImageInputStream iis = ImageIO.createImageInputStream(ip);
		reader = (ImageReader) ImageIO.getImageReaders(iis).next();
		reader.setInput(iis);
		BufferedImage image = reader.read(0);
		iis.close();

		// Scale the image to around 160x120/120x160 pixels, may not conform
		// exactly to Thumbnail requirements of 160x120.
		int t, longer, shorter;
		longer = image.getWidth();
		shorter = image.getHeight();
		if (shorter > longer) {
			t = longer;
			longer = shorter;
			shorter = t;
		}
		double factor = 160 / (double) longer;
		double factor1 = 120 / (double) shorter;
		if (factor1 > factor)
			factor = factor1;
		AffineTransform tx = new AffineTransform();
		tx.scale(factor, factor);
		AffineTransformOp affineOp = new AffineTransformOp(tx,
				AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = affineOp.filter(image, null);

		// Write Out the Scaled Image to a ByteArrayOutputStream and return the
		// bytes
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2048);
		String format = "JPG";
		ImageIO.write(image, format, byteStream);

		return byteStream.toByteArray();
	}
}

