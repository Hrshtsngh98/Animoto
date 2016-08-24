import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Scanner;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;

public class AnimotoClient {
	static AmazonSQS sqs;
	static GetQueueUrlRequest qur;
	static String url,x,bucketname="harshit123";
	static BufferedImage image = null;
	static int count=0;
	private static S3Object s3object;//declating variables tp create bucket request
	private static BufferedReader br;
	public static void main(String[] args) throws Exception {
		Scanner s=new Scanner(System.in);
		System.out.println("Enter Url File address");
		String add=s.next();
		br = new BufferedReader(new FileReader(add));//buffer to take in put address
		AmazonS3Client S3Client=new AmazonS3Client(new ProfileCredentialsProvider());	//creating amazonS3client to connect to amazon s3 for upading video
		S3Client.createBucket(bucketname);////creating a bucket or drive on S3 to put the video
		int count=0;
		InputStream is;
		File f;
		OutputStream os;
		long start=System.currentTimeMillis();//taking initial time
		System.out.println("Downloading Images...\n");	//printing that image are being downlaoded
		while((x=br.readLine())!=null) 
		{
			count++;
			try{
			String imageUrl = x;	//storing the picked up url from the file
			String destinationFile = count+".jpg";	//assigning the destination of the downlaoded image
			URL url = new URL(imageUrl);	//converting the picked string of url to URL 
			is = url.openStream();	//opening a downlaod stream for a file
			os = new FileOutputStream(destinationFile);	//creating a stream to write to destination
			byte[] b = new byte[4096];	//creating a buffer for current image being download
			int length;
			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);}		//writing buffer to final image file
			is.close();//saving the image file
			os.close();
			f=new File(destinationFile);
			}
			catch(Exception e){e.printStackTrace();}
		}
		System.out.println("Combining images to video\n");	
		String command="ffmpeg -f image2 -i ./%d.jpg ./Animoto.mpg";//the command to be executed to to create video file
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);//executing the bash command of ffmpeg to create the video file
			System.out.println(p);
			p.waitFor();//waiting for the command to execute and video completely made
		} catch (Exception e) {
			e.printStackTrace();
		}
		f=new File("Animoto.mpg");//getting the object of the ceated file
		System.out.println("Pushing video to S3 Bucket\n");
		S3Client.putObject(new PutObjectRequest(bucketname, "./"+f.getName(), f));	//putting the created file to the amazon bucket
		long end=System.currentTimeMillis();//finding the end time
		System.out.println("Total time taken in milliseconds = "+(end-start));//calculating the total time taken
	}
}
