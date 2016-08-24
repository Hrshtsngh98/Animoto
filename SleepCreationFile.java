import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SleepCreationFile {
	public static void main(String[] args) throws IOException {
		BufferedWriter bw=new BufferedWriter(new FileWriter(new File("Input.txt")));
		
		for(int i=1;i<10000;i++)
		{
			bw.write("sleep 0\n");
		}
		bw.write("sleep 0");
		bw.flush();
		bw.close();
	}

}
