import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class LocalQueue {
static Queue LocalQ=new LinkedList();//creating the queue to perform tasks
static int n,r=0,f=0;
static String x;//creating variables for further use
static Scanner s;
static BufferedReader br;
static File file;
static long start,end,total;
public LocalQueue(String fname,int n) throws Exception 
{
	file=new File(fname);//taking input of sleep file
	br=new BufferedReader(new FileReader(file));//input from the sleep file
	start=System.currentTimeMillis();//statring time 
	Thread cl=new Thread(new Client());//creating a client
	cl.start();//starting a client
	for(int i=0;i<n;i++)
	{Thread wo=new Thread(new Worker());
	wo.start();//starting a worker
	wo.join();//synchronizing the workers
	}
	System.out.println("Total time taken in milliseconds= "+total);
	total=0;
}
public static class Client implements Runnable{
	public void run(){//implement threading 
			try {
				while((x=br.readLine())!=null)//reading the task from teh file
				{
				LocalQ.add(x);//adding the task to the que for worker
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
public static class Worker implements Runnable{
	public synchronized void run(){//implement threading 
		try{
			while(!(x=(String) LocalQ.poll()).isEmpty()){//checking if queue is empty
			if(x.split(" ")[0].equals("sleep"))//splitting the task adn performing sleep task
			Thread.sleep(Integer.parseInt(x.split(" ")[1]));
			} 
		}
		catch (Exception e) {}
		end=System.currentTimeMillis(); //finding the time 
		total=total+end-start;//calculatinf the time
		}
	}
}
