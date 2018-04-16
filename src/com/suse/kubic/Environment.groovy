package com.suse.kubic;

import net.sf.json.JSONObject;

// A Kubic Environment
class Environment implements Serializable {
    String dashboardHost;
    String kubernetesHost;

    String sshUser;
    String sshKey;

    List<Minion> minions = new ArrayList<>();

    Environment() {}

    Environment(JSONObject environmentJson) {
        // Fill out the dashboardHost and kubernetesHost
        this.dashboardHost = environmentJson.dashboardHost
        this.kubernetesHost = environmentJson.kubernetesHost
        this.sshUser = environmentJson.sshUser
        this.sshKey = environmentJson.sshKey

        environmentJson.minions.each { tfMinion ->
            Minion minion = new Minion()

            minion.index = tfMinion.index
            minion.fqdn = tfMinion.fqdn
            minion.role = tfMinion.role
            minion.minionId = tfMinion.minionId
            minion.proxyCommand = tfMinion.proxyCommand
            minion.status = tfMinion.status
            minion.addresses.publicIpv4 = tfMinion.addresses.publicIpv4
            minion.addresses.privateIpv4 = tfMinion.addresses.privateIpv4

            this.minions.push(minion)
        }
    }
}
