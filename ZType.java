import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.Random;


// Represents a word in the ZType game
class Word {
  String text;
  int x;
  int y;
  Color color;

  Word(String text, int x, int y, Color color) {
    this.text = text;
    this.x = x;
    this.y = y;
    this.color = color;
  }

  // Moves a word down one in the y direction
  Word move() {
    return new Word(this.text, this.x, this.y + 1, this.color);
  }

  // checks to confirm that the key inputed matches the first letter of the word
  boolean check(String key) {
    return this.text.startsWith(key) && !this.text.isEmpty();
  }
  
  //Removes the first character and turns green
  Word removeFirstChar() {
    if (this.text.length() <= 1) {
      return null;
    } 
    else {
      return new Word(this.text.substring(1), this.x, this.y, Color.GREEN);
    }
  }

  // checks to see if a word has reached the bottom of the screen
  boolean reachedBottom() {
    return this.y >= 605; // this might need to be 0 i lowkey cant tell
  }

  // created a world scene for the text image of the single word
//  WorldScene draw(WorldScene scene) {
//    TextImage wordImage = new TextImage(this.text, Color.WHITE);
//    return scene.placeImageXY(wordImage, this.x, this.y);
//  }
  WorldScene draw(WorldScene scene) {
    return scene.placeImageXY(new TextImage(this.text, 14, Color.BLACK), this.x, this.y); //Added size
  }
}

interface ILoWord {
  
  // moves all of the words on the screen
  ILoWord moveAll();
  
  // checks to see if the input key matches, then removes the letter that matched
  ILoWord checkAndRemove(String key);
  
  // confirms whether or not the word has reached the bottom of the screen
  boolean reachedBottom();
  
  // draws the worldscene for each word and each movement that occurs
  WorldScene draw(WorldScene scene);

  ILoWord replace(Word oldWord, Word newWord);

  ILoWord removeWord(Word oldWord);
  
}

// represents an empty list of words
class MtLoWord implements ILoWord {
  
  MtLoWord () {
  }
  
  // moves all the words in an empty list (nothing should happen)
  public ILoWord moveAll() { 
    return this; 
  }
  
  // checks and removes all the words in an empty list (nothing should happen)
  public ILoWord checkAndRemove(String key) { 
    return this; 
  }
  
  // checks whether or not a words has reached the bottom (nothing should happen)
  public boolean reachedBottom() { 
    return false; 
  }
  
  // draws the worldscene for the words in the empty list (nothing should happen)
  public WorldScene draw(WorldScene scene) { 
    return scene; 
  }

  public ILoWord replace(Word oldWord, Word newWord) {
    return new MtLoWord();
  }

  public ILoWord removeWord(Word oldWord) {
    return new MtLoWord();
  }
  
}

// Represents a list of words in the ZType game
class ConsLoWord implements ILoWord {
  Word first;
  ILoWord rest;

  ConsLoWord(Word first, ILoWord rest) {
    this.first = first;
    this.rest = rest;
  }

  // moves all the words down in a list of words
  public ILoWord moveAll() {
    return new ConsLoWord(this.first.move(), this.rest.moveAll());
  }

  // checks and removes properly input keys in a list of words
  public ILoWord checkAndRemove(String key) {
    if (this.first.check(key)) {
        Word newWord = this.first.removeFirstChar();
        if (newWord == null) {
            return this.rest.checkAndRemove(key); // Remove the word entirely
        } else {
            return new ConsLoWord(newWord, this.rest); // Update the word
        }
    } else {
        return new ConsLoWord(this.first, this.rest.checkAndRemove(key)); // No match
    }
  }

  // checks whether a word has reached the bottom of the screen in a list of words
  public boolean reachedBottom() {
    return this.first.reachedBottom() || this.rest.reachedBottom();
  }

  // draws the worldscene for each word in a list of words
  public WorldScene draw(WorldScene scene) {
    return this.rest.draw(this.first.draw(scene));
  }

  public ILoWord replace(Word oldWord, Word newWord) {
    if (this.first == oldWord) {
        return new ConsLoWord(newWord, this.rest);
    } else {
        return new ConsLoWord(this.first, this.rest.replace(oldWord, newWord));
    }
  }

  public ILoWord removeWord(Word oldWord) {
    if (this.first == oldWord) {
        return this.rest;
    } else {
        return new ConsLoWord(this.first, this.rest.removeWord(oldWord));
    }
  }
}

//Represents the ZType game world
class ZTypeWorld extends World {
  ILoWord words;
  Random rand;
  int ticks;
  Word focus;
  
  ZTypeWorld(ILoWord words, Random rand, int ticks) {
    this.words = words;
    this.rand = rand;
    this.ticks = ticks;
    this.focus = null;
  }
  
  ZTypeWorld(Random rand) {
    this.rand = rand;
    this.ticks = 0;
    this.words = new MtLoWord();//Start with an empty list
    this.words = generateInitialWords(3);
  }
  
  ZTypeWorld(ILoWord words, int ticks, Word focus) {
    this.words = words;
    this.ticks = ticks;
    this.focus = focus;
  }
  
  ILoWord generateInitialWords(int count) {
    if (count == 0) {
        return new MtLoWord(); // Base case: no more words
    } else {
        String newText = generateRandomWord(6);
        int x = generateRandomX();
        Word newWord = new Word(newText, x, 0, Color.WHITE);
        return new ConsLoWord(newWord, generateInitialWords(count - 1)); // Recursive call
    }
  }
  
  int generateRandomX() {
    return rand.nextInt(700);
  }
  
  //Draw all words on the scene
  public WorldScene makeScene() {
    return this.words.draw(new WorldScene(800, 600));
  }
  
  // Add a new word every 20 ticks and move existing words down
  public World onTick() {
    ILoWord movedWords = this.words.moveAll();
    int newTicks = this.ticks + 1;
    ILoWord newWords = movedWords;
  
    if (newTicks % 20 == 0) { // Add a new word every 20 ticks
      String newText = this.generateRandomWord(6);
      int x = this.rand.nextInt(700); // Random x within scene width
      newWords = new ConsLoWord(new Word(newText, x, 0, Color.WHITE), movedWords);
    }
    return new ZTypeWorld(newWords, this.rand, newTicks);
  }
  
  // For testing: Use a seeded random to generate predictable words
  public ZTypeWorld onTickForTesting() {
    ILoWord movedWords = this.words.moveAll();
    Random testRand = new Random(42); // Fixed seed for testing
    String newText = this.generateRandomWord(6, testRand);
    int x = testRand.nextInt(700);
    ILoWord newWords = new ConsLoWord(new Word(newText, x, 0, Color.WHITE), movedWords);
    return new ZTypeWorld(newWords, testRand, this.ticks + 1);
  }
  
  // Handle key presses: Remove first character of matching words
  public World onKeyEvent(String key) {
    if (this.focus != null) {
        if (this.focus.check(key)) {
            Word newWord = this.focus.removeFirstChar();
            if (newWord == null) {
                ILoWord newWords = this.words.removeWord(this.focus);
                return new ZTypeWorld(newWords, this.ticks, null); // Word completed!
            } else {
                ILoWord newWords = this.words.replace(this.focus, newWord);
                return new ZTypeWorld(newWords, this.ticks, newWord); // Update the list
            }
        } else {
            return this; // Ignore incorrect key presses when focused
        }
    } else {
        ILoWord checkedWords = this.words.checkAndRemove(key);

        if (checkedWords instanceof ConsLoWord && ((ConsLoWord) checkedWords).first.check(key)) {
            this.focus = ((ConsLoWord) checkedWords).first; // Focus on the first matching word
            return new ZTypeWorld(checkedWords, this.ticks, this.focus);
        } else {
          return new ZTypeWorld(checkedWords, this.ticks, null);
        }
    }
  }
  
  //Helper function to replace a word in the list
  ILoWord replace(Word oldWord, Word newWord) {
    if (this.words instanceof MtLoWord) {
      return new MtLoWord();
    } else {
      ConsLoWord consWords = (ConsLoWord) this.words;
      if (consWords.first == oldWord) {
        return new ConsLoWord(newWord, consWords.rest);
      } else {
        return new ConsLoWord(consWords.first, consWords.rest.replace(oldWord, newWord));
      }
    }
  }
  
  //Helper function to replace a word in the list
  ILoWord removeWord(Word oldWord) {
    if (this.words instanceof MtLoWord) {
      return new MtLoWord();
    } else {
      ConsLoWord consWords = (ConsLoWord) this.words;
      if (consWords.first == oldWord) {
        return  consWords.rest;
      } else {
        return new ConsLoWord(consWords.first, consWords.rest.removeWord(oldWord));
      }
    }
  }
  
  // End game if any word reaches the bottom
  public WorldEnd worldEnds() {
    if (this.words.reachedBottom()) {
      return new WorldEnd(true, this.makeFinalScene());
    }
    return new WorldEnd(false, this.makeScene());
  }
  
  // Game over screen
  public WorldScene makeFinalScene() {
    WorldScene scene = new WorldScene(800, 600);
    TextImage gameOver = new TextImage("Game Over!", Color.RED);
    return scene.placeImageXY(gameOver, 400, 300);
  }
  
  // Generate random word (instance method)
  String generateRandomWord(int length) {
    return generateRandomWord(length, this.rand);
  }
  
  // Helper with explicit Random for testing
  String generateRandomWord(int length, Random r) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append((char) (r.nextInt(26) + 'a'));
    }
    return sb.toString();
  }
}

// a class that works to generate a new random word
class WordGenerator {
  Random rand = new Random();
  String generateRandomWord(int length) {
    return new String(rand.ints(length, 'a', 'z' + 1).toArray(), 0, length);
  }
}

//Test examples and test cases
class ExamplesZType {
  Random rand = new Random(42); // Fixed seed for testing
  Word w1 = new Word("test", 100, 100, Color.WHITE);
  Word w2 = new Word("abcd", 200, 200, Color.WHITE);
  ILoWord mtWords = new MtLoWord();
  ILoWord list1 = new ConsLoWord(w1, mtWords);
  ILoWord list2 = new ConsLoWord(w2, list1);
  
  ZTypeWorld initWorld = new ZTypeWorld(rand);
  ZTypeWorld testWorld = new ZTypeWorld(list2, rand, 0);
  
  // Test word movement
  boolean testMove(Tester t) {
    return t.checkExpect(w1.move(), new Word("test", 100, 101, Color.WHITE))
        && t.checkExpect(list1.moveAll(), new ConsLoWord(w1.move(), mtWords));
  }
  
  // Test key handling
  boolean testKeyPress(Tester t) {
    Word w1_after_key = new Word("est", 100, 100, Color.GREEN); // Correctly updated word
    ILoWord expected_list = new ConsLoWord(w2, new ConsLoWord(w1_after_key, mtWords));
    return t.checkExpect(list2.checkAndRemove("t"), expected_list);
  }
  
  // Test game end condition
  boolean testGameOver(Tester t) {
    Word bottomWord = new Word("end", 400, 605, Color.WHITE);
    ILoWord bottomList = new ConsLoWord(bottomWord, mtWords);
    ZTypeWorld endingWorld = new ZTypeWorld(bottomList, rand, 0);
    WorldEnd expectedEnd = new WorldEnd(true, endingWorld.makeFinalScene()); // Create expected WorldEnd
    return t.checkExpect(endingWorld.worldEnds(), expectedEnd); // Compare the entire WorldEnd
  }
  
  // Run the game
  boolean testBigBang(Tester t) {
    ZTypeWorld world = new ZTypeWorld(new Random()); //Use a new random
    return world.bigBang(800, 600, 1.0 / 30.0); // Width, Height, Tick Rate (30 FPS)
  }
}