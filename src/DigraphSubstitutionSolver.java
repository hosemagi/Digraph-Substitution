import java.util.Collections;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class DigraphSubstitutionSolver
{
	private DigraphFrequencyFinder frqFinder;
	private String cipherText;
	private String cipherTextFilePath;
	private String shortCipher;
	private HashMap<String, ArrayList<String>> mutations;
	private HashMap<String, ArrayList<String>> startingMutations;
	private HashMap<String, String> translationTable;
	private double bestScore;
	private HashMap<String, String> bestTable;
	private int mutationCount;
	private int startingMutationCount;
	
	public DigraphSubstitutionSolver()
	{
		this.frqFinder = null;
		this.cipherText = null;
	}
	
	public DigraphSubstitutionSolver(String cipherTextFilePath)
	{	
		this.frqFinder = null;
		this.loadCipherText(cipherTextFilePath);
	}
	
	public void loadCipherText(String filepath)
	{
		try
		{
			int count = 0;
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			this.cipherText = "";
			String line = reader.readLine();
			while(line != null)
			{
				this.cipherText += line;
				line = reader.readLine();
			}
			reader.close();
			
			System.out.println("Successfully loaded ciphertext from file " + filepath + ". Ciphertext length " + this.cipherText.length() + " characters.");
			this.cipherTextFilePath = filepath;
			this.shortCipher = this.cipherText.substring(500000, Math.min(504000, this.cipherText.length()));
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public void loadFrequencyTable(String filepath)
	{
		this.frqFinder = new DigraphFrequencyFinder(filepath);
		this.frqFinder.loadFrequencyList();
	}
	
	public void simpleSolve()
	{
		DigraphFrequencyFinder observedFrq = new DigraphFrequencyFinder("observedFrequencies_"+this.cipherTextFilePath);
		observedFrq.consumeFile(this.cipherTextFilePath, 2);
		observedFrq.saveFrequencyList();
		
		ArrayList<Digram> observed = new ArrayList<Digram>();
		ArrayList<Digram> expected = new ArrayList<Digram>();
		
		for(int i = 0; i < 26; i++)
		{
			for(int j = 0; j < 26; j++)
			{
				String bigram = (char)(i + 97) + "" + (char)(j + 97);
				double obsFreq = observedFrq.getFrequencyForBigram(bigram);
				double expFreq = this.frqFinder.getFrequencyForBigram(bigram);
				
				observed.add(new Digram(bigram, obsFreq));
				expected.add(new Digram(bigram, expFreq));
			}
		}
		
		Collections.sort(observed);
		Collections.sort(expected);
		this.translationTable = new HashMap<String, String>();
		
		for(int i = 0; i < observed.size(); i++)
		{
			System.out.println();
			System.out.println(observed.get(i).toString());
			System.out.println(expected.get(i).toString());
			
			translationTable.put(observed.get(i).text, expected.get(i).text);
		}
		
		String plaintext = this.decipher(this.shortCipher, this.translationTable);
		
		System.out.println("First guess:");
		System.out.println(plaintext);
		
		this.generatePossibleMutations(observed, expected);
		this.bestScore = this.plainTextScore(this.decipher(this.shortCipher, this.translationTable));
		System.out.println("Starting score: " + this.bestScore);
		while(this.canMutate())
		{
			//if(this.mutate())
			//	this.generatePossibleMutations(observed, expected);
			this.mutate();
		}
	}
	
	public void generatePossibleMutations(ArrayList<Digram> observed, ArrayList<Digram> expected)
	{
		this.mutationCount = 0;
		this.mutations = new HashMap<String, ArrayList<String>>();
		for(int i = 0; i < observed.size(); i++)
		{
			String obsDigraph = observed.get(i).text;
			for(int j = 0; j < expected.size(); j++)
			{
				String expDigraph = expected.get(j).text;
				if(Math.abs(observed.get(i).frequency - expected.get(j).frequency) < 0.0065)
				{
					if(observed.get(i).frequency > 0.0030)
					{
						ArrayList<String> list = this.mutations.get(obsDigraph);
						if(list == null)
							list = new ArrayList<String>();
						list.add(expDigraph);
						this.mutations.put(obsDigraph, list);
						this.mutationCount++;
					}
				}
			}
		}
		this.startingMutations = this.mutations;
		this.startingMutationCount = this.mutationCount;
		System.out.println("Generated " + this.mutationCount + " possible mutations");
	}
	
	public boolean mutate()
	{
		boolean mutated = false;
		Iterator<String> it = this.mutations.keySet().iterator();
		String key = it.next();
		ArrayList<String> list = this.mutations.get(key);
		String val = list.get(0);
		list.remove(0);
		this.mutations.put(key, list);
		if(list.size() == 0)
			this.mutations.remove(key);
		
		String oldValue = this.translationTable.get(key);
		this.translationTable.put(key, val);
		String plaintext = this.decipher(this.shortCipher, this.translationTable);
		double score = this.plainTextScore(plaintext);
		if(score > bestScore)
		{
			bestScore = score;
			bestTable = this.translationTable;
			
			System.out.println("Successful mutation. "+key+":"+oldValue + "=>"+val+  " Score: " + this.bestScore );
			System.out.println(this.decipher(this.shortCipher, this.bestTable));
			mutated = true;
		}
		else
		{
			//System.out.println(key + "=>" + oldValue + "\t" + key + "=>" + val);
			this.translationTable.put(key, oldValue);
		}
		
		if(this.mutationCount % 1000 == 0)
			System.out.println("Remaining mutations: " + this.mutationCount + " score: " + score);
		this.mutationCount--;
		
		return mutated;
	}
	
	public String decipher(String text, HashMap<String, String> trans)
	{
		String plaintext = "";
		for(int i = 0; i < text.length() - 1; i += 2)
			plaintext += trans.get(text.substring(i, i+2));
		return plaintext;
	}
	
	public boolean likelyPlayfair(String text)
	{
		for(int i = 0; i < text.length() - 1; i += 2)
		{
			if(text.charAt(i) == text.charAt(i+1))
			{
				System.out.println(text.substring(i, i+20));
				return false;
			}
		}
		return true;
	}
	
	public boolean canMutate()
	{
		if(this.mutations.size() > 0)
			return true;
		return false;
	}
	
	public double plainTextScore(String text)
	{
		double score = 0;
		double[] freqs = this.getLetterFrequencies(text);
		score += Math.max(150 - this.getXSQFrequencyScore(freqs, 0, text.length()), 0);
		
		HashMap<String, Integer> trigraphCounts = new HashMap<String, Integer>();
		trigraphCounts.put("the", 0);
		trigraphCounts.put("and", 0);
		trigraphCounts.put("tha", 0);
		trigraphCounts.put("ent", 0);
		trigraphCounts.put("ion", 0);
		trigraphCounts.put("tio", 0);
		trigraphCounts.put("for", 0);
		trigraphCounts.put("nde", 0);
		trigraphCounts.put("has", 0);
		trigraphCounts.put("nce", 0);
		trigraphCounts.put("edt", 0);
		trigraphCounts.put("tis", 0);
		trigraphCounts.put("oft", 0);
		trigraphCounts.put("sth", 0);
		trigraphCounts.put("men", 0);
		trigraphCounts.put("are", 0);
		trigraphCounts.put("for", 0);
		trigraphCounts.put("but", 0);
		trigraphCounts.put("not", 0);
		trigraphCounts.put("all", 0);
		trigraphCounts.put("any", 0);
		trigraphCounts.put("how", 0);
		trigraphCounts.put("too", 0);
		trigraphCounts.put("use", 0);
		trigraphCounts.put("let", 0);
		trigraphCounts.put("put", 0);
		trigraphCounts.put("out", 0);
		trigraphCounts.put("our", 0);
		trigraphCounts.put("day", 0);
		
		for(int i = 0; i < text.length() - 2; i++)
		{
			String key = text.substring(i, i+3);
			if(trigraphCounts.containsKey(key))
			{
				int count = trigraphCounts.get(key);
				count++;
				trigraphCounts.put(key, count);
				score++;
			}
		}
		
		return score;
	}
	
	/*****************************
	 * 
	 *	Returns the ratio of occurrences of 'letter' in 'str' to length of 'str'
	 *	as a double value 0 <= x <= 1
	 * 
	 *****************************/
	public double getLetterFrequency(char letter, String str)
	{
		int count = 0;
		for(int i = 0; i < str.length(); i++)
			if(str.charAt(i) == letter)
				count++;
		return (double)Math.round((((double)count/(double)str.length()) * 1000))/1000;
	}


	/*****************************
	 * 
	 *	Returns a Chi-Squared statistic that indicates the strength with which the
	 *	specified observed frequencies (frequencies[]) correspond to the expected frequencies
	 *	(letterFrequencies[]). 
	 * 
	 * The shift parameter optionally allows to perform the test on a shift of the observed frequencies. Otherwise should pass 0.
	 * 
	 * The length is the length of the original text the frequencies occurred in.
	 * 
	 * NOTE: Lower chi-squared values indicate stronger correspondence between observed/expected frequencies
	 *****************************/
	public double getXSQFrequencyScore(double[] frequencies, int shift, int length)
	{
		double[] letterFrequencies = new double[26];
		letterFrequencies[0]	= .08167;
		letterFrequencies[1]	= .01492;
		letterFrequencies[2]	= .02782;
		letterFrequencies[3]	= .04253;
		letterFrequencies[4]	= .12702;
		letterFrequencies[5]	= .02228;
		letterFrequencies[6]	= .02015;
		letterFrequencies[7]	= .06094;
		letterFrequencies[8]	= .06966;
		letterFrequencies[9]	= .00153;
		letterFrequencies[10]	= .00772;
		letterFrequencies[11]	= .04025;
		letterFrequencies[12]	= .02406;
		letterFrequencies[13]	= .06749;
		letterFrequencies[14]	= .07507;
		letterFrequencies[15]	= .01929;
		letterFrequencies[16]	= .00095;
		letterFrequencies[17]	= .05987;
		letterFrequencies[18]	= .06327;
		letterFrequencies[19]	= .09056;
		letterFrequencies[20] 	= .02758;
		letterFrequencies[21] 	= .00978;
		letterFrequencies[22] 	= .02360;
		letterFrequencies[23] 	= .00150;
		letterFrequencies[24] 	= .01974;
		letterFrequencies[25] 	= .00074;

		//perform shift
		double[] shifted = new double[26];
		for(int i = 0; i < 26; i++)
		{
			shifted[i] = frequencies[(i + shift) % 26];
		}

		//determine chi squared test statistc
		double xsq = 0;

		for(int i = 0; i < frequencies.length; i++)
		{
			xsq += Math.pow((double)(shifted[i]*length - letterFrequencies[i]*length), 2)/(double)(letterFrequencies[i]*length);
		}

		return xsq;
	}
	
	/*****************************
	 * 
	 *	returns a double[26] of frequencies for each letter (a..z) in the string 'text'
	 * 
	 *****************************/
	public double[] getLetterFrequencies(String text)
	{
		double[] freqs = new double[26];
		for(int i = 0; i < 26; i++)
			freqs[i] = this.getLetterFrequency((char)(97 + i), text);
		return freqs;
	}
}