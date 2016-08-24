import java.util.Scanner;

public class Menu {
private static Scanner s;

public static void main(String[] args) throws Exception {
	s = new Scanner(System.in);//declaring the main java to run any type of client
	String command[]=new String[10];
	System.out.println("Enter command or Quit to exit");
	command=s.nextLine().split(" ");//taking command as an input and storing
	while(!command[0].equalsIgnoreCase("quit"))//iterating for multiple commads
	{
		if(command[0].equals("client")&&command[1].equals("-s")//checking for the command
				&&command[3].equals("-w")){
			new SQSClient(command[2],command[4]);//creating the remote queue
		}
		else if(command[0].equals("client")&&command[1].equals("-s")
				&&command[2].equals("LOCAL")&&command[3].equals("-t")
				&&command[5].equals("-w")){		//checking for the command
			new LocalQueue(command[6],Integer.parseInt(command[4]));//creating the local queue
		}
		else
			System.err.println("Wrong Command");
		System.out.println("Enter command or Quit to exit");
		command=s.nextLine().split(" ");
	}
}
}
