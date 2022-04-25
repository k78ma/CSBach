package contrapunctus;

import org.jfugue.*;

import java.io.File;
import java.util.*;
import static contrapunctus.Melody.*;

public class AppMain {
	
	public static void main(String[] args) throws Throwable{
		
		// The random used throughout the program
		Random rand = new Random();
		
		Object[] ns = matchRhythm(getMelody(rand).toArray(new String[0]),rand);
		String[] cp = matchCounterpoint((String[])ns[0], (Integer)ns[1], rand);
		StringBuffer sb_cp = new StringBuffer("V1 I0\n");
		
		StringBuffer sb_ml = new StringBuffer("Tempo[400] I0 V0\n"); // this sets a tempo
		for(String note : (String[])ns[0]) sb_ml.append(note + " ");
		for(String note : cp) sb_cp.append(note + " ");
		sb_ml.append(sb_cp);
		
		System.out.println(sb_ml.toString()); // this is a string of notes
		Player player = new Player(); // this plays music.
		player.play(sb_ml.toString());
		
				
	}
}
