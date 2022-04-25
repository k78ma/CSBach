package contrapunctus;

import static contrapunctus.Notes.*;
import static contrapunctus.Probability.DOWN; // this imports static contrathingy
import static contrapunctus.Probability.cumSum; // this imports static contrathingy
import static contrapunctus.Probability.normalizeWeights; // this imports static contrathingy

import java.util.*; // here's the rest of the code. it basically lets you use code from like different programs... something about usage and happiness

// Methods that generate melodies of a random length.
public class Melody {

	// Generate a random melody without rhythm, starting and ending at C.
	static ArrayList<String> getMelody(Random rand){
		String n = "C5";
		ArrayList<String> ns = new ArrayList<String>();
		ns.add(n);
		
		int notesMade = 0;
		
		// Very rudimentary probabilistic chain approach
		while(true){
			
			// Weights array. Indices 1-8 are weights for notes upward and
			// 9-16 are weights for notes downward. Index 0 is same note.
			double[] weights = new double[17];
			weights[0] = 10;
			weights[1] = 50;
			weights[1 + DOWN] = 50;
			weights[2] = 10;
			weights[2 + DOWN] = 10;
			weights[3] = 8;
			weights[3 + DOWN] = 8;
			weights[4] = 8;
			weights[4 + DOWN] = 8;
			weights[7] = 4;
			weights[7 + DOWN] = 4;
			
			// Avoid tritone with F-B
			if(extractNoteBase(n).equals("F")) weights[3] = 0;
			if(extractNoteBase(n).equals("B")) weights[3+DOWN] = 0;
			
			// Minor six possible when ascending
			if(extractNoteBase(n).equals("E") ||
				extractNoteBase(n).equals("A") ||
				extractNoteBase(n).equals("B")) weights[5] = 6;
			
			int distanceC = noteDistance("C5", n); //Distance from note to C5
			double hookelaw = Math.pow(1.18, Math.abs(distanceC));
			
			//Decreases the probability that the program will continuously go back and forth two notes; reduces probability of return but not too much
			if(distanceC > 0)
			for(int k = 1; k <= 8; k++){
				weights[k] /= (k*hookelaw / 2);
			}
			if(distanceC < 0) // this checks distance and stuff.
			for(int k = 1+DOWN; k <= 8+DOWN; k++){
				weights[k] /= (k*hookelaw / 2);
			}
			
			normalizeWeights(weights); // this normalizes weights.
			
			// Cumulative weights, for RNG
			double[] cum = cumSum(weights);
			
			String nn = n; // next note
			
			// Generate the next note based on the above probabilities
			boolean found_flag = false;
			double r = rand.nextDouble();
			if(r < cum[0]) {nn = n; found_flag = true;} //If the random number generated is than the weight of 0, play 0
			for(int k = 1; k <= 8; k++){ // If the random number generated is less than a given interval (only up to 8 to somewhat observe voice leading rules), play that note
				if(r < cum[k]){
					nn = scaleNote(n,k);
					found_flag = true;
					break;
				}
			}
			if(!found_flag)
			for(int k = 1; k <= 8; k++){ //Same thing for descendings
				if(r < cum[k+DOWN]){
					nn = scaleNote(n,-k);
					found_flag = true;
					break;
				}
			}
			
			n = nn;
			ns.add(nn);
			
			notesMade ++;
			
			// Resolve to a leading tone if we have enough notes and
			// we have a B or a D.
			if(notesMade > 30 && (n.equals("B4") || n.equals("D5"))) break; //After 40 notes, if there's a B4 or a D5, resolve to C5
		}
		ns.add("C5"); // adds a C5 thing
		return ns;
	}
	
	// 4 2 time because 8 2 is just double 4 2
	static final String[] RHYTHMS = {
		"hhhh", "ww", "whh", "hhw"
	};
	
	// Given a melody, generate a rhythm for it.
	// This method returns an array of objects, the first index being the list of note
	// strings, and the second object being the number of bars in the song.
	static Object[] matchRhythm(String[] notes, Random rand){
		
		String[] ret = new String[notes.length]; // Create list of notes (with rhythms)
		int p = notes.length - 1;
		ret[p] = notes[p] + "ww"; //Add whole note C at the end
		p--;
		
		int bars = 1;
		
		while(p >= 0){
			String rhm = RHYTHMS[rand.nextInt(RHYTHMS.length)]; //Choose a rhythm from the "RHYTHMS" string for each note
			int str_p = 0;
			while(p >= 0 && str_p < rhm.length()){
				ret[p] = notes[p] + rhm.charAt(str_p); //Adding note + rhythm associated with note to the ret[p] list defined above
				str_p++; 
				p--;
			}
			bars++; //Add to bar count every time
		}
		
		return new Object[]{ret,bars}; // returns a new object from a bunch of bars and rests
	}
	
	// Two methods to extract the rhythm and non-rhythm part of a note.
	static char extractRhythm(String note){
		return note.charAt(note.length() - 1);
	}
	
	static String extractNonRhythm(String note){
		return note.substring(0, note.length() - 1); //returns a substring of notes of certain lengths
	}
	// // //
	
	// Given an existing melody, generate reasonable-ish counterpoint!
	static String[] matchCounterpoint(String[] melody, int len, Random rand){
		
		// We match the counterpoint in three separate parts.
		// 1: Generate a rhythm of the desired length
		// 2: For each note that we have (that we don't have the tone for, only the
		//     duration), match it up with one note that it's supposed to
		//     harmonize with.
		// 3: Finally, generate the tones so that they harmonize with the first
		//      relevant note (dissonance after that is allowed).
		// After that we will reorder to return a nice array of notes.
		
		String[] rhm = new String[len]; //Define a new string of rhythms
		rhm[0] = "ww"; //Start at 0
		for(int bars = 1; bars < len; bars++) //Create random rhythms for counterpoint string
			rhm[bars] = RHYTHMS[rand.nextInt(RHYTHMS.length)];
		
		// Keep track of the position we are at within the melody.
		int pmelody;
		pmelody = melody.length - 2; // Last note is already used.
		
		// Part two: match each counterpoint note with a harmony. Note that everything
		// including this array list is structured backwards.
		ArrayList<String> matches = new ArrayList<String>();
		
		// The final note of the counterpoint matches this note
		matches.add("C5");
		
		// Match the notes one bar at a time.
		for(int bar = 1; bar < len; bar++){
			
			// This is so confusing:
			// ths_rhythm goes FORWARDS in time
			// beat goes BACKWARDS in time
			// match goes BACKWARDS
			
			char[] ths_rhythm = rhm[bar].toCharArray(); //Array of rhythms
			int tb_p = 0;
			int beat = 0; // 4 2 time, so reset when this is 4.
			
			// Know which note is playing on each beat
			String[] beats = new String[4];
			while(beat < 4 && pmelody >= 0){
				if(extractRhythm(melody[pmelody]) == 'h'){
					beats[beat] = extractNonRhythm(melody[pmelody]); //Extract the note on that beat for a half note
					beat ++;
				}
				if(extractRhythm(melody[pmelody]) == 'w'){ //Extract the note on that beat for a whole note (2 beats)
					beats[beat] = extractNonRhythm(melody[pmelody]);
					beats[beat+1] = extractNonRhythm(melody[pmelody]);
					beat += 2;
				}
				pmelody --;
			}
			
			// Match this with our given rhythm
			//System.out.println(Arrays.toString(beats));
			//System.out.println(Arrays.toString(ths_rhythm));
			
			ArrayList<String> mat_reverse = new ArrayList<String>();
			
			beat = 3;
			while(beat >= 0){ // goes backwards on beat but forwards in time
				if(beats[beat] != null) // handles start of song
					mat_reverse.add(beats[beat]);
				if(ths_rhythm[tb_p] == 'h'){
					beat -= 1;
				}
				if(ths_rhythm[tb_p] == 'w'){
					beat -= 2;
				}
				tb_p ++; // position in the counterpoint bar
			}
			Collections.reverse(mat_reverse);
			matches.addAll(mat_reverse);
			
		}
		
		
		// We have the harmony matches. Now let's generate the melody.
		ArrayList<String> counterpoint = new ArrayList<String>();
		counterpoint.add("C4ww");
		
		// Merge all the rhythms and reverse them
		StringBuffer buildstr = new StringBuffer();
		for(int i = 1; i < len; i++){
			//buildstr.append(rhm[i]);
			buildstr.append(new StringBuilder(rhm[i]).reverse().toString());
		}
		String s_rhms = buildstr.toString();
		
		
		// Generate a counterpoint melody. The last note as well as the first notes
		// are already determined. The only difference from here to before is that
		// now we are generating everything backwards.
		String n = "C4";
		int nt;
		
		for(nt = 1; nt < matches.size() - 1; nt++){
			//counterpoint.add(transNote(matches.get(nt),-12) + s_rhms.charAt(nt-1));
			
			// Below is code that is mostly copy and pasted from above. The major difference
			// is we have to check until we find a note that harmonizes, or put a break.
			
			String nn = n; // next note
			
			int tries = 0;
			while(true){
				
				// Weights array. Indices 1-8 are weights for notes upward and
				// 9-16 are weights for notes downward. Index 0 is same note.
				double[] weights = new double[17];
				weights[0] = 10;
				weights[1] = 50;
				weights[1+DOWN] = 50;
				weights[2] = 10;
				weights[2+DOWN] = 10;
				weights[3] = 8;
				weights[3+DOWN] = 8;
				weights[4] = 8;
				weights[4+DOWN] = 8;
				weights[7] = 4;
				weights[7+DOWN] = 4;
				
				// Avoid tritone with F-B
				if(extractNoteBase(n).equals("F")) weights[3] = 0;
				if(extractNoteBase(n).equals("B")) weights[3+DOWN] = 0;
				
				// Minor six possible when ascending
				if(extractNoteBase(n).equals("C") ||
					extractNoteBase(n).equals("F") ||
					extractNoteBase(n).equals("G")) weights[5] = 6;
				
				int distanceC = noteDistance("C4", n);
				double hookelaw = Math.pow(1.18, Math.abs(distanceC));
				
				if(distanceC > 0)
				for(int k = 1; k <= 8; k++){
					weights[k] /= (k*hookelaw / 2);
				}
				
				if(distanceC < 0) // this checks distance.
				for(int k = 1+DOWN; k <= 8+DOWN; k++){
					weights[k] /= (k*hookelaw / 2);
				}
				
				normalizeWeights(weights); // this normalizes weights.
				
				// Cumulative weights, for RNG
				double[] cum = cumSum(weights);
				
				// Generate the next note based on the above probabilities
				boolean found_flag = false;
				double r = rand.nextDouble();
				if(r < cum[0]) {nn = n; found_flag = true;}
				for(int k=1; k<=8; k++){
					if(r < cum[k]){
						nn = scaleNote(n,k);
						found_flag = true;
						break;
					}
				}
				if(!found_flag)
				for(int k = 1; k <= 8; k++){
					if(r < cum[k+DOWN]){
						nn = scaleNote(n,-k);
						found_flag = true;
						break;
					}
				}
				
				if(canHarmonize(nn, matches.get(nt), rand)) break;
				else tries++;
			}
			
			n = nn;
			counterpoint.add(nn + s_rhms.charAt(nt-1));
			//if(tries > 10)
			//	System.out.println(tries);
		}
		
		// A slightly large hack is required:
		int bts = 0;
		for(String s : melody){
			if(extractRhythm(s)=='w') bts += 2;
			else bts ++;
		}
		for(String s : counterpoint){
			if(extractRhythm(s)=='w') bts -= 2;
			else bts --;
		}
		
		if(bts == 1) counterpoint.add("C4h");
		else if(bts == 2) counterpoint.add("C4w");
		else if(bts == 3) counterpoint.add("C4w.");
		
		Collections.reverse(counterpoint); // collects a reversed counterpoint
		return counterpoint.toArray(new String[0]);
	}
	
}
