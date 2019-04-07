import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class WordCookies 
{
	final static int MAX_WORD_SIZE = 6;
	final static int MIN_WORD_SIZE = 4;
	
	public static void main(String[] args) 
	{
		String mainWord;
		String[] validWords;
		String[] blankWords;
		boolean[] alreadyGuessed; //keeps track of which words have been guessed correctly
		ArrayList<String> dictionary = new ArrayList<String>(169000);
		boolean done = false;
		boolean allDone = true; //set to false if not all of alreadyGuessed is true
		String guess;
		
		//Set up game
		dictionary = loadDictionary();
		mainWord = mainWordPicker(dictionary);
		validWords = wordFinder(dictionary, mainWord);		
		validWords = wordOrderer(validWords);
		
		alreadyGuessed = alreadyGuessedSetup(validWords);

		blankWords = arraySetup(validWords);
		printInfo();
		mainWord = scramble(mainWord);
		printBoard(mainWord, blankWords);
		
		//main loop	- continue looping until all words have been guessed	
		while (done == false)
		{
			allDone = true;
			guess = guessWord(validWords, blankWords, alreadyGuessed);
			
			if (guess.equalsIgnoreCase("s"))
			{
				mainWord = scramble(mainWord);
			}
			else if (guess.equalsIgnoreCase("h"))
			{
				blankWords = hint(blankWords, validWords);
			}
						
			printBoard(mainWord, blankWords);
			
			for (int x = 0; x < alreadyGuessed.length; x++)
			{
				if (alreadyGuessed[x] == false)
				{
					allDone = false;
				}
			}
			
			if (allDone == true)
			{
				done = true;
			}
		}
		System.out.println("You won!  Thanks for playing!");
	}
	
	//selects mainWord randomly from the dictionary
	public static String mainWordPicker(ArrayList<String> dictionary)
	{
		int size = dictionary.size();
		Random r = new Random();
		String mainWord = "";
		
		while (mainWord.length() > MAX_WORD_SIZE || mainWord.length() < MIN_WORD_SIZE)
		{
			mainWord = dictionary.get(r.nextInt(size));
		}
		return mainWord;
	}
	
	//puts the words in validWords in order by length and then position in alphabet
	public static String[] wordOrderer(String[] validWords)
	{
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<String> tempBuild = new ArrayList<String>(validWords.length);
		int longest = 0;
		
		//find length of longest word in validWords
		for (int x = 0; x < validWords.length; x++)
		{
			if (validWords[x].length() > longest)
			{
				longest = validWords[x].length();
			}
		}
				
		for (int x = 1; x <= longest; x++)
		{
			//remove all words of x length and add to temp
			for (int y = 0; y < validWords.length; y++)
			{
				if (validWords[y].length() == x)
				{
					temp.add(validWords[y]);
				}
			}
			//sort temp alphabetically and add to tempBuild
			Collections.sort(temp);
			
			for (int z = 0; z < temp.size(); z++)
			{
				tempBuild.add(temp.get(z));
			}
			
			temp.clear();
		}
		
		//turn tempBuild into validWords, which is a string array
		validWords = tempBuild.toArray(validWords);
		
		return validWords;
	}
	
	//sets up alreadyGuessed with the same amount of cells as validWords
	public static boolean[] alreadyGuessedSetup (String[] validWords)
	{
		boolean[] alreadyGuessed = new boolean[validWords.length];
		Arrays.fill(alreadyGuessed, false);
		return alreadyGuessed;
	}
	
	//scrambles the passed String (which will be mainWord in the main function)
	public static String scramble (String word)
	{
		char[] arr = word.toCharArray();
		Random r = new Random();
		int y;
		char temp;
		
		for (int x = 0; x < arr.length; x++)
		{
			y = r.nextInt(arr.length);
			
			temp = arr[x];
			arr[x] = arr[y];
			arr[y] = temp;
		}
		
		System.out.println("Word scrambled.");
		return new String(arr);	
	}
	
	//gives a one-letter hint. replaces first - in blankWords with corresponding validWords letter
	public static String[] hint (String[] blankWords, String[] validWords)
	{
		for (int x = 0; x < blankWords.length; x++)
		{
			for (int y = 0; y < blankWords[x].length(); y++)
			{
				if (blankWords[x].charAt(y) == '-')
				{
					blankWords[x] = blankWords[x].substring(0, y) + validWords[x].charAt(y) + 
							blankWords[x].substring(y + 1);
					System.out.println("Hint given.");
					return blankWords;
				}
			}
		}
		//If all hints are given:
		System.out.println("No more hints are available.");
		return blankWords;
	}
	
	//Lets user guess words and handles input accordingly
	public static String guessWord(String[] validWords, String[] blankWords, boolean[] alreadyGuessed)
	{
		String guess;
		Scanner scan = new Scanner(System.in);
		
		System.out.print("Please enter a guess: ");
		guess = scan.nextLine();
		guess = guess.toUpperCase(); //dictionary of words is in uppercase
		
		//if user asks to shuffle word
		if (guess.equalsIgnoreCase("*s"))
		{
			return "s";
		}
		
		//if user asks for a hint
		else if (guess.equalsIgnoreCase("*h"))
		{
			return "h";
		}
		
		//otherwise, assume user entered guess
		else if (containsString(guess, validWords) == true)
		{			
			System.out.println(guess + " is correct!");			
				/* when user guesses correctly:
				 * make the corresponding cell in alreadyGuessed true
				 * take the word from validWords and put it into the blankWords cell of the same #
				 * */
			for (int x = 0; x < validWords.length; x++)
			{
				if (validWords[x].equals(guess))
				{
					blankWords[x] = validWords[x];
					alreadyGuessed[x] = true;
				}
			}
		}
		else
		{
			System.out.println(guess + " is incorrect.");
		}
		return "go";
	}
	
	//Finds words using the letters in the main word. Returns String array of said words
	//Returned array will become validWords
	public static String[] wordFinder(ArrayList<String> dictionary, String mainWord)
	{
		ArrayList<String> validWords = new ArrayList<String>(5);
		boolean contain = true; //keeps track of if dictionary word is made of letters in mainWord
		String testWord;
		String testMainWord;
		int index;
		
		for (int x = 0; x < dictionary.size(); x++) //change this to dictionary
		{
			/* goes through each letter of each word in dictionary to see if the word is
			 * made up of letters in mainWord*/
			testWord = dictionary.get(x).toUpperCase();
			testMainWord = mainWord.toUpperCase();
			contain = true;
						
			for (int y = 0; y < dictionary.get(x).length(); y++)
			{				
				if (testMainWord.contains(Character.toString(testWord.charAt(0))) == true)
				{
					index = testMainWord.indexOf(testWord.charAt(0));
					testMainWord = testMainWord.substring(0, index) + testMainWord.substring(index+1);
					testWord = testWord.substring(1);
				}
				else
				{
					//testMainWord does not contain a character in testWord
					//therefore, testWord is not made entirely from characters in mainWord
					contain = false;
				}
			}
			
			if (contain == true)
			{
				validWords.add(dictionary.get(x));
			}
		}
		
		return arrayListToArray(validWords);
	}

	//Returns a String array that has blanks corresponding to the words in validWords
	public static String[] arraySetup(String[] validWords)
	{
		String[] blankWords = new String[validWords.length];
		
		for (int x = 0; x < validWords.length; x++)
		{
			blankWords[x] = "-";
			
			//this loop starts at 1 because there is already one hyphen in the cell
			//without the first hyphen, the string is not initialized, so hyphens can't be added to it
			for (int y = 1; y < validWords[x].length(); y++)
			{
				blankWords[x] = blankWords[x] + "-";
			}
		}
		
		return blankWords;
	}
	
	//Prints blankWords and mainWord after each guess
	public static void printBoard(String mainWord, String[] blankWords)
	{
		System.out.println();
		
		System.out.println("Board:");
		for (int x = 0; x < blankWords.length; x++)
		{
			System.out.println(blankWords[x]);
		}
		
		System.out.println();
		
		System.out.println("Word: " + mainWord.toUpperCase());
		
		System.out.println();
	}
	
	//load dictionary.txt into an ArrayList
	public static ArrayList<String> loadDictionary()
	{
		Path filePath = Paths.get("dictionary.txt");
		File file = filePath.toFile();
		ArrayList<String> dictionary = new ArrayList<String>(169000);
		
		try (BufferedReader input = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = input.readLine()) != null)
			{
				dictionary.add(line);
			}
			input.close();
		}
		catch (IOException e)
		{
			System.out.println("Couldn't open dictionary.txt! Obscure information follows...");
			System.out.println(e);
		}
		return dictionary;
	}
	
	//prints beginning info
	public static void printInfo()
	{
		System.out.println("Welcome to Word Cookies, by Melody Gill!");
		System.out.println("The object of this game is to guess words that can be made out of");
		System.out.println("the same letters as a word you will be given.");
		System.out.println("For example, 'dog' can be made out of 'good'.");
		System.out.println("Enter *s to scramble the given word.");
		System.out.println("Enter *h for a hint.");
		System.out.println("Enjoy!");
	}
	
	//helps with guessWord. Returns true if a String is in the array
	public static boolean containsString (String guess, String[] validWords)
	{
		for (int x = 0; x < validWords.length; x++)
		{
			if (validWords[x].equalsIgnoreCase(guess))
			{
				return true;
			}
		}
		return false;
	}
	
	//for testing purposes. Prints the passed array, one line per cell
	public static void arrayPrinter(String[] array)
	{
		for (int x = 0; x < array.length; x++)
		{
			System.out.println(array[x]);
		}
	}
	
	//same as above but prints an ArrayList
	public static void arrayPrinter(ArrayList<String> arrLi)
	{
		for (int x = 0; x < arrLi.size(); x++)
		{
			System.out.println(arrLi.get(x));
		}
	}
	
	//Converts ArrayList to String array
	public static String[] arrayListToArray (ArrayList<String> arrLi)
	{
		String[] newArray = new String[arrLi.size()];
		
		for (int x = 0; x < arrLi.size(); x++) 
		{
			newArray[x] = arrLi.get(x);
		}
		
		return newArray;
	}
}
