
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSWorker implements Runnable {
	//declaring various variables for further use
	static AmazonSQS sqs,sqs2;//Amazon SQS client declaration 
	static List<Message> arr,messages;//declaring list to save data
	static String m,x,url=null,url2=null;//declaring url variables
	static Iterator<Item> iterator;
	static Table table=null;
	static DynamoDB dynamoDB;//Declaring dynamodb Client
	static GetQueueUrlRequest qur,qur2;//creating url request vvarible
	private static Scanner s;
	static int count=0;
	static String qname;
	static ReceiveMessageRequest receiveMessageRequest;//creating SQS receive Message Request
	public synchronized void run()
	{
		SendMessageRequest smr=new SendMessageRequest();//creating SQS send Message Request
		try{		
		qur=new GetQueueUrlRequest(qname);	//getting the URL of the queue by clientby its name
		qur2=new GetQueueUrlRequest("Receiver"+qname);
		do
		{
			url=sqs.getQueueUrl(qur).getQueueUrl();//getting url of the que again
			url2=sqs.getQueueUrl(qur).getQueueUrl();
			table=dynamoDB.getTable(qname);//getting the web address of the table
			table.waitForActive();//waitin for the dynamodb table to be active or else there will be error
		}while(url==null && url2==null && table == null);
		ReceiveMessageRequest receiveMessageRequest=new ReceiveMessageRequest(url);//creating SQS receive Message Request from URL
		receiveMessageRequest.setMaxNumberOfMessages(10);//creating SQS receive Message Request
		System.out.println("Receiving messages from Queue ");
		while((arr=sqs.receiveMessage(receiveMessageRequest).getMessages()).size()!=0){
			messages.addAll(arr);
			sqs.deleteMessage(new	//creating a receipt handle for the queue from which messges are being received
				DeleteMessageRequest().withQueueUrl(url).withReceiptHandle(arr.get(0).getReceiptHandle()));
		}
		System.out.println();
		for (Message message : messages) 
		{	
		    m= message.getBody();
		    x = message.getMessageId();
		    try{
		    	table.putItem(new Item().withPrimaryKey("MessageID",x).with("Message",m));//putting messages to table to check if they already exist
		    	count++;
		    	Thread.sleep(Integer.parseInt((m.split(" ")[1])));//executing the sleep task
		    	sqs2.sendMessage(smr.withQueueUrl(url2).withMessageBody("Done"));//sending the done tak message to the queue 2
		    }
		    catch(Exception e){
		    }
		}
		}
		catch(Exception e){
		e.printStackTrace();
		}
	}
	public static void main(String ar[]) throws Exception {
		messages=new ArrayList<>();
		arr=new ArrayList<>();
		sqs=new AmazonSQSClient();//creating the SQS client to connect
		sqs2=new AmazonSQSClient();
		dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
	            new ProfileCredentialsProvider()));//creating the dynamodb cleint
		s = new Scanner(System.in);
		String command[]= new String[10];
		System.out.println("Enter Command");
		command=s.nextLine().split(" ");// getting the commad from the user
		if(command[0].equals("worker")&&command[1].equals("-s")&&command[3].equals("-t"))//checking if the input commands to be correct and findin condition in them
		{
			int i,N=Integer.parseInt(command[4]);
			qname=command[2];
			for(i=0;i<N;i++)
			{
				Thread t=new Thread(new SQSWorker());//creating new worker
				t.start();
			}
		}
		else
			System.err.println("Wrong Command");
		table.delete();
		sqs.deleteQueue(url);
		sqs.deleteQueue(url2);	
		System.out.println("Task Done");
	}
}
