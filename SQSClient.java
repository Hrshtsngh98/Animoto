
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSClient {
	static BufferedReader br;
	static File f;//creating variables for further use
	static ReceiveMessageRequest receiveMessageRequest;
	static List<Message> arr,messages;//creating variables for further use
	static CreateQueueRequest createQueueRequest,createQueueRequest2;//creating SQS receive Message Request
	static AmazonSQS sqs;//Amazon SQS client declaration 
	static String url1,url2,x;//creating URL
	static long start,end,total=0L;//creating variables for further use
	public SQSClient(String Qname,String fname) throws Exception {
		f=new File(fname);
		br=new BufferedReader(new FileReader(f));
		System.out.println("Creating a new Sender and Receiver SQS queue...\n");
		createQueueRequest = new CreateQueueRequest().withQueueName(Qname);//starting a new queu to send
		createQueueRequest2 = new CreateQueueRequest().withQueueName(("Receiver"+Qname));//starting new quueue to receive
		SendMessageRequest smr=new SendMessageRequest();//creating SQS send Message Request
		ReceiveMessageRequest receiveMessageRequest=new ReceiveMessageRequest(url2);//creating SQS receive Message Request
		sqs = new AmazonSQSClient();//Amazon SQS client declaration 
		url1 = sqs.createQueue(createQueueRequest).getQueueUrl();//getting the URL of the queue by clientby its name
		url2 = sqs.createQueue(createQueueRequest2).getQueueUrl();//getting the URL of the queue by clientby its name
		int count=0;//decclatig messgae count of sent and received
		DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
	            new ProfileCredentialsProvider()));
		Table table=null;
		try {
			ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();//the column declaration is in the kind of Array list
            attributeDefinitions.add(new AttributeDefinition()
                .withAttributeName("MessageID")
                .withAttributeType("S"));//declaring table with teh coloumn name Message ID
            attributeDefinitions.add(new AttributeDefinition()//The coloumn type is the string type
                    .withAttributeName("Message")//declaring table with teh coloumn name Message ID
                    .withAttributeType("S"));//The coloumn type is the string type
            ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement()
                .withAttributeName("MessageID")//confirming name of the column that is the schema
                .withKeyType(KeyType.HASH)); //Partition key
            keySchema.add(new KeySchemaElement()
                    .withAttributeName("Message")//confirming name of the column that is the schema
                    .withKeyType(KeyType.RANGE));//naormal variable
            CreateTableRequest request = new CreateTableRequest()
                .withTableName(Qname)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()//declarin teh size of the buffe of messags
                    .withReadCapacityUnits(16L)
                    .withWriteCapacityUnits(16L));
            System.out.println("Creating a new DynamoDB table....\n");//printing messege of source code
            table = dynamoDB.createTable(request);//creating a dynamo db table
            table.waitForActive();//waitng for the table to be active
		
		System.out.println("Sending a message to Queue.\n");
		start=System.currentTimeMillis();//starting the timer
		while((x=br.readLine())!=null) 
		{
			sqs.sendMessage(smr.withQueueUrl(url1).withMessageBody(x));//sending messge to 1st queue the sleep task
			count++;
		}
		int n=count;//storing the message sent count
		System.out.println("Sent Message Count = "+count+"\n");//printing tje sent messages count
		count=0;
		}catch(Exception e){e.printStackTrace();
		}
		System.out.println("Waiting for messages to receive\n");//printinf the message
		count=0;
		try{
			while((arr=sqs.receiveMessage(receiveMessageRequest).getMessages()).size()!=0){
				sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(url2)//deleting the messages from teh queue on receiving 
					.withReceiptHandle(arr.get(0).getReceiptHandle()));//with the help of receipt handle
				count=count+arr.size();//count number of messages received
			}
		}
		catch(Exception e){e.printStackTrace();}
		end=System.currentTimeMillis();// stopping the timer
		System.out.println("Received Message Count = "+count+"\n");//printint receive count
		System.out.println("Total time in millis = "+(end-start));//printing time taken
	}
}
