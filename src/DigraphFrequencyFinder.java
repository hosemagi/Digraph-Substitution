import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class DigraphFrequencyFinder
{
	private String frequencyListFilePath;
	private HashMap<String, Integer> frequencies;
	
	public DigraphFrequencyFinder(String frequencyListFilePath)
	{
		this.frequencyListFilePath = frequencyListFilePath;
		this.frequencies = new HashMap<String, Integer>();
	}
	
	public void loadFrequencyList()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(this.frequencyListFilePath));
			String line = reader.readLine();
			while(line != null)
			{
				String[] tokens = line.split("=");
				if(tokens[0].equals("total"))
				{
					int total = Integer.parseInt(tokens[1]);
					this.frequencies.put("total", total);
				}
				else
				{
					String digraph = tokens[0];
					int count = Integer.parseInt(tokens[1]);
					this.frequencies.put(digraph, count);
				}
				line = reader.readLine();
			}
			reader.close();
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public void loadFrequencyList(String listFilePath)
	{
		this.frequencyListFilePath = listFilePath;
		this.loadFrequencyList();
	}
	
	public void saveFrequencyList()
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.frequencyListFilePath));
			Iterator<String> it = this.frequencies.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				int value = this.frequencies.get(key);
				writer.write(key + "=" + value + "\r\n");
			}
			writer.close();
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public void consumeFile(String filepath, int skip)
	{
		try
		{
			String text = "";
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			String line = reader.readLine();
			while(line != null)
			{
				text += line;
				line = reader.readLine();
			}
			reader.close();
			this.analyzeText(text, skip);
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public void analyzeText(String text, int skip)
	{
		int total = 0;
		if(this.frequencies.get("total") != null)
			total = this.frequencies.get("total");
		for(int i = 0; i < text.length()-1; i += skip)
		{
			String digraph = text.substring(i, i+2);
			int count = 0;
			if(this.frequencies.get(digraph) != null)
				count = this.frequencies.get(digraph);
			count++;
			this.frequencies.put(digraph, count);
			total++;
			this.frequencies.put("total", total);
		}
		this.saveFrequencyList();
	}
	
	public double getFrequencyForBigram(String bigram)
	{
		int count = 0;
		if(this.frequencies.get(bigram) != null)
			count = this.frequencies.get(bigram);
		int total = this.frequencies.get("total");
		double freq = (double)count/(double)total;
		return freq;
	}
}