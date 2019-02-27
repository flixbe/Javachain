package core;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import sandbox.Javachain;
import utils.StringUtil;

public class Transaction {

	public String transactionID;
	public PublicKey sender;
	public PublicKey reciepient;
	public float value;
	public byte[] signature;
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0;
	
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}

	private String calculateHash() {
		sequence++;
		return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value) + sequence);
	}

	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}
	
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
		
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	public boolean processTransaction() {
		if (!verifySignature()) {
			System.out.println("#Transaction Signature failed to verify!");
			return false;
		}
		
		for (TransactionInput i : inputs)
			i.UTXO = Javachain.UTXOs.get(i.transactionOutputID);
		
		if (getInputsValue() < Javachain.minimumTransaction) {
			System.out.println("#Transaction Inputs to small: " + getInputsValue());
			return false;
		}
		
		float leftOver = getInputsValue() - value;
		transactionID = calculateHash();
		outputs.add(new TransactionOutput(this.reciepient, value, transactionID));
		outputs.add(new TransactionOutput(this.sender, value, transactionID));
		
		for (TransactionOutput o : outputs)
			Javachain.UTXOs.put(o.ID, o);
		
		for (TransactionInput i : inputs) {
			if (i.UTXO == null) 
				continue;
			Javachain.UTXOs.remove(i.UTXO.ID);
		}
		
		return true;
	}

	public float getInputsValue() {
		float total = 0;
		for (TransactionInput i : inputs) {
			if (i.UTXO == null)
				continue;
			total += i.UTXO.value;
		}
		
		return total;
	}
	
	public float getOutputsValue() {
		float total = 0;
		for (TransactionOutput o : outputs) 
			total += o.value;
		
		return total;
	}
	
}