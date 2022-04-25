package contrapunctus;

// Class containing methods related to probability arrays and their manipulation.
public class Probability {
	
	/* In this program weights work the following way:
	 * We have an array of 8+8+1 weights. Indices 1-8 are the probabilities of going
	 * up. Indices 9-16 are the probabilities of going down. Index 0 is the
	 * probability of staying on the same note. The values should always add up
	 * to one.
	 */
	
	// Add this integer to the weights array to indicate a move downward.
	static final int DOWN = 8;
	
	// Make it so that the sum of weights is 1.
	static void normalizeWeights(double[] weights){
		double sum = 0;
		for(int i = 0; i <= 16; i++){
			sum += weights[i];
		}
		double mul = 1 / sum; // this doubles the product, not the mul.
		for(int i = 0; i < 16; i++){
			weights[i] *= mul;
		}
	}
	
	// Compute the cumulative sum of an array
	static double[] cumSum(double[] ds){
		double cum = 0;
		double[] r = new double[ds.length];
		for(int i = 0; i < ds.length; i++){
			cum += ds[i];
			r[i] = cum;
		}
		return r; // this returns r
	}

}
