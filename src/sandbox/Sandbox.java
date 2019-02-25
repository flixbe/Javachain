package sandbox;

import java.util.ArrayList;
import com.google.gson.GsonBuilder;

import com.google.gson.GsonBuilder;

import core.Block;

public class Sandbox {

	public static final int DIFFICULTY = 5;
	public static ArrayList<Block> blockchain = new ArrayList<Block>();

	public static void main(String[] args) {
		blockchain.add(new Block("First block", "0"));
		System.out.println("Trying to Mine block 1... ");
		blockchain.get(0).mineBlock(DIFFICULTY);
		
		blockchain.add(new Block("Second block",blockchain.get(blockchain.size()-1).hash));
		System.out.println("Trying to Mine block 2... ");
		blockchain.get(1).mineBlock(DIFFICULTY);
		
		blockchain.add(new Block("Third block",blockchain.get(blockchain.size()-1).hash));
		System.out.println("Trying to Mine block 3... ");
		blockchain.get(2).mineBlock(DIFFICULTY);	
		
		System.out.println("\nBlockchain is Valid: " + isChainValid());
		
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson);
	}

	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[DIFFICULTY]).replace('\0', '0');		
		
		for (int i = 1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i - 1);

			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("Current Hashes not equal");
				return false;
			}

			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			
			if (!currentBlock.hash.substring(0, DIFFICULTY).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}
		}
		return true;
	}
}