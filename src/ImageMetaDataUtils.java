import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import mediautil.gen.directio.SplitInputStream;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.IFD;
import mediautil.image.jpeg.LLJTran;

public class ImageMetaDataUtils 
{
	public static boolean addEXIF(String pathSource, String pathTarget)
	{
		try 
		{
			String pi = pathSource;
			String po = pathTarget;

			InputStream fip = new FileInputStream(pi); // No need to buffer
			SplitInputStream sip = new SplitInputStream(fip);
			
			InputStream subIp = sip.createSubStream();
			LLJTran llj = new LLJTran(subIp);

			llj.initRead(LLJTran.READ_HEADER, true, true);
			sip.attachSubReader(llj, subIp);

			sip.wrapup();
			sip.close();
			fip.close();

			// Check llj for errors
			String msg = llj.getErrorMsg();
			if (msg != null) 
			{
				System.out.println("Error in LLJTran While Loading Image: " + msg);
				
				Exception e = llj.getException();
				if (e != null) 
				{
					throw e;
				}
				return false;
			}

			// Exif
			AbstractImageInfo<?> imageInfo = llj.getImageInfo();
			if (!(imageInfo instanceof Exif)) 
			{
				System.out.println("Adding a Dummy Exif Header");
				llj.addAppx(LLJTran.dummyExifHeader, 0,	LLJTran.dummyExifHeader.length, true);
				imageInfo = llj.getImageInfo();
			}
			Exif exif = (Exif) imageInfo;
			
			changeAritstAndCopyright(exif, "1111", "2222");
	        		
			// OUTPUT
			fip = new BufferedInputStream(new FileInputStream(pi));
			OutputStream out = new BufferedOutputStream(new FileOutputStream(po));

			llj.refreshAppx();
			llj.xferInfo(fip, out, LLJTran.REPLACE, LLJTran.RETAIN);

			fip.close();
			out.close();

			llj.freeMemory(); // Cleanup
			
			File file = new File(pi);
			file.delete();
			
			return true;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static void changeAritstAndCopyright(Exif exif, String artist, String copyright)
	{
        IFD mainIfd = exif.getIFDs()[0];

        Entry e = new Entry(Exif.ASCII);
        e.setValue(0, artist);
        mainIfd.setEntry(Exif.ARTIST, 0, e);
        
        e = new Entry(Exif.ASCII);
        e.setValue(0, copyright);
        mainIfd.setEntry(Exif.COPYRIGHT, 0, e);
	}
	
	public static void changeMakeAndModel(Exif exif, String make, String model)
	{		
		Entry entry = exif.getTagValue(Exif.MAKE, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, make);
		}		
		
		entry = exif.getTagValue(Exif.MODEL, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, model);
		}
	}
	

	public static void changeDateTime(Exif exif, String datetime)
	{		 
		Entry entry = exif.getTagValue(Exif.DATETIME, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, datetime);
		}
		
		entry = exif.getTagValue(Exif.DATETIMEORIGINAL, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, datetime);
		}
		
		entry = exif.getTagValue(Exif.DATETIMEDIGITIZED, true);
		if (entry != null)
		{
			System.out.println(">" + entry.getValue(0));
			entry.setValue(0, datetime);
		}
	}
	
	public static void outputAllExif(Exif exif)
	{
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
	}
}
