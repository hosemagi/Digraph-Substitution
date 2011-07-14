import java.util.Collections;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DigraphSubstitutionSolver
{
	private DigraphFrequencyFinder frqFinder;
	private String cipherText;
	private String cipherTextFilePath;
	
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
		HashMap<String, String> translationTable = new HashMap<String, String>();
		
		for(int i = 0; i < observed.size(); i++)
		{
			System.out.println();
			System.out.println(observed.get(i).toString());
			System.out.println(expected.get(i).toString());
			
			translationTable.put(observed.get(i).text, expected.get(i).text);
		}
		
		String plaintext = "";
		for(int i = 0; i < cipherText.length()-1; i += 2)
		{
			System.out.print(translationTable.get(this.cipherText.substring(i, i+2)));
		}
		
		System.out.println(plaintext);
	}
}