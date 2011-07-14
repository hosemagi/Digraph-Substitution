public class Digram implements Comparable<Digram>
{
	public double frequency;
	public String text;
	
	public Digram(String text)
	{
		this.text = text;
	}
	
	public Digram(String text, double frequency)
	{
		this.text = text;
		this.frequency = frequency;
	}
	
	public int compareTo(Digram d)
	{
		if(this.frequency - d.frequency > 0)
			return 1;
		else if(this.frequency - d.frequency < 0)
			return -1;
		else
			return 0;
	}
	
	public String toString()
	{
		return "Text: " + this.text + "\t" + "Freq: " + this.frequency;
	}
}