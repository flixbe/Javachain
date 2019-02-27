package core;

import java.util.ArrayList;
import java.util.Date;

import utils.StringUtil;

public class Block {

	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	
	public long timeStamp;
	public int nonce;
	public String hash;
	public String previousHash;
	public String merkleRoot;
	
	public Block(String previousHash) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}
	
	public String calculateHash() {
		return StringUtil.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
	}
	
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty);
		
		while (!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		
		System.out.println("Block Mined: " + hash);
	}
	
	public boolean addTransaction(Transaction transaction) {
		if (transaction == null)
			return false;
		
		if ((previousHash != "0")) {
			if (!transaction.processTransaction()) {
				System.out.println("Transaction failed to precess. Discarded.");
				return false;
			}
		}
		
		transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		
		return true;
	}
	
}