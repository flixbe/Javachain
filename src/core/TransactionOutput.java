package core;

import java.security.PublicKey;

import utils.StringUtil;

public class TransactionOutput {

	public String ID;
	public String parentTransactionID;
	public PublicKey reciepient;
	public float value;
	
	public TransactionOutput(PublicKey reciepeint, float value, String parentTransactionID) {
		this.reciepient = reciepeint;
		this.value = value;
		this.parentTransactionID = parentTransactionID;
		this.ID = StringUtil.applySha256(StringUtil.getStringFromKey(reciepeint) + Float.toString(value) + parentTransactionID);
	}
	
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
}
