package sandbox;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.gson.GsonBuilder;

import core.Block;
import core.Transaction;
import core.TransactionInput;
import core.TransactionOutput;
import core.Wallet;

public class Javachain {

	public static final int DIFFICULTY = 6;

	public static float minimumTransaction = 0.1f;

	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

	public static Wallet firstWallet;
	public static Wallet secondWallet;

	public static Transaction genesisTransaction;

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		createTransaction();
		testing();
		isChainValID();
	}

	public static void createTransaction() {
		firstWallet = new Wallet();
		secondWallet = new Wallet();
		Wallet coinbase = new Wallet();
		
		genesisTransaction = new Transaction(coinbase.publicKey, firstWallet.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);	
		genesisTransaction.transactionID = "0";
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionID));
		UTXOs.put(genesisTransaction.outputs.get(0).ID, genesisTransaction.outputs.get(0));
	}
	
	public static void testing() {
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		mineBlocks();
		
		Block firstBlock = new Block(genesis.hash);
		System.out.println("\nfirstWallet's balance is: " + firstWallet.getBalance());
		System.out.println("\nfirstWallet is Attempting to send funds (40) to secondWallet...");
		firstBlock.addTransaction(firstWallet.sendFunds(secondWallet.publicKey, 40f));
		addBlock(firstBlock);
		System.out.println("\nfirstWallet's balance is: " + firstWallet.getBalance());
		System.out.println("secondWallet's balance is: " + secondWallet.getBalance());
		
		Block secondBlock = new Block(firstBlock.hash);
		System.out.println("\nfirstWallet Attempting to send more funds (1000) than it has...");
		secondBlock.addTransaction(firstWallet.sendFunds(secondWallet.publicKey, 1000f));
		addBlock(secondBlock);
		System.out.println("\nfirstWallet's balance is: " + firstWallet.getBalance());
		System.out.println("secondWallet's balance is: " + secondWallet.getBalance());
		
		Block thirdBlock = new Block(secondBlock.hash);
		System.out.println("\nsecondWallet is Attempting to send funds (20) to firstWallet...");
		thirdBlock.addTransaction(secondWallet.sendFunds( firstWallet.publicKey, 20));
		System.out.println("\nfirstWallet's balance is: " + firstWallet.getBalance());
		System.out.println("secondWallet's balance is: " + secondWallet.getBalance());		
	}
	
	public static void mineBlocks() {
		 blockchain.add(new Block("0"));
		 System.out.println("Trying to Mine first block ... ");
		 blockchain.get(0).mineBlock(DIFFICULTY);
		
		 blockchain.add(new Block(blockchain.get(blockchain.size() - 1).hash));
		 System.out.println("Trying to Mine second block ... ");
		 blockchain.get(1).mineBlock(DIFFICULTY);
		
		 blockchain.add(new Block(blockchain.get(blockchain.size() - 1).hash));
		 System.out.println("Trying to Mine third block ... ");
		 blockchain.get(2).mineBlock(DIFFICULTY);
		
		 System.out.println("\nBlockchain is ValID: " + isChainValID());
		 String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		 System.out.println("\nThe block chain: ");
		 System.out.println(blockchainJson);		
	}
	
	public static Boolean isChainValID() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[DIFFICULTY]).replace('\0', '0');
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).ID, genesisTransaction.outputs.get(0));

		for (int i = 1; i < blockchain.size(); i++) {

			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i - 1);

			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("#Current Hashes not equal!");
				return false;
			}

			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("#Previous Hashes not equal!");
				return false;
			}

			if (!currentBlock.hash.substring(0, DIFFICULTY).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined!");
				return false;
			}

			TransactionOutput tempOutput;
			for (int t = 0; t < currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);

				if (!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is InvalID!");
					return false;
				}
				
				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")!");
					return false;
				}

				for (TransactionInput input : currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputID);

					if (tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing!");
						return false;
					}

					if (input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is InvalID");
						return false;
					}

					tempUTXOs.remove(input.transactionOutputID);
				}

				for (TransactionOutput output : currentTransaction.outputs)
					tempUTXOs.put(output.ID, output);

				if (currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				
				if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}

			}

		}
		System.out.println("Blockchain is valID");
		return true;
	}

	public static void addBlock(Block block) {
		block.mineBlock(DIFFICULTY);
		blockchain.add(block);
	}
}