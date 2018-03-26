package com.suse.kubic;

class OpenstackTypeOptions implements Serializable {
	String image = null;
	String channel = 'devel';
	String openrcCredentialId = 'prvcld-openrc-caasp-jenkins-tests-1';
	String adminFlavor = 'm1.large';
	String masterFlavor = 'm1.large';
	String workerFlavor = 'm1.large';
}
