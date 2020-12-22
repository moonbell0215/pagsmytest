package com.dht.pags.wallet.materalizedviewprocesor.domain;

public class Balance {

    private String id;
    private String employeecode;
    private Double balance;
    private String updatetime;

    public Balance(String id, String employeecode, Double balance, String updatetime) {
        this.id = id;
        this.employeecode = employeecode;
        this.balance = balance;
        this.updatetime = updatetime;
    }

    public Balance() {
    }

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmployeecode() {
		return employeecode;
	}

	public void setEmployeecode(String employeecode) {
		this.employeecode = employeecode;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public String getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	@Override
    public String toString() {
        return String.format("%s %s, %s", employeecode, balance, updatetime);
    }
}
