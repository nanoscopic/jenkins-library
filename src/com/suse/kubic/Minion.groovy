package com.suse.kubic;

class Minion implements Serializable {
	String index;
	String fqdn;
	String role;
	String minionId;
	String proxyCommand;
	String status;
	Addresses addresses = new Addresses();
}
