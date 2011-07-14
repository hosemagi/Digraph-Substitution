import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class Main
{
	public static void main(String[] args)
	{
		DigraphSubstitutionSolver solver = new DigraphSubstitutionSolver("cipher.txt");
		solver.loadFrequencyTable("DigraphFrequencies.dat");
		solver.simpleSolve();
	}
}

