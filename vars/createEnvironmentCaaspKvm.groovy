// Copyright 2017 SUSE LINUX GmbH, Nuernberg, Germany.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
import com.suse.kubic.Environment
import com.suse.kubic.CaaspKvmTypeOptions


Environment call(Map parameters = [:]) {
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')

    CaaspKvmTypeOptions options = parameters.get('typeOptions', null)

    if (options == null) {
        options = new CaaspKvmTypeOptions()
    }

    Environment environment

    def proxyFlag = ""
    if (env.hasProperty("http_proxy")) {
        proxyFlag = "-P ${env.http_proxy}"
    }

    def vanillaFlag = ""
    if (options.vanilla) {
        vanillaFlag = "--vanilla"
    }

    def disableMeltdownSpectreFixesFlag = ""
    if (options.disableMeltdownSpectreFixes) {
        disableMeltdownSpectreFixesFlag = "--disable-meltdown-spectre-fixes"
    }

    def velumImage = "channel://${options.channel}"
    if (options.velumImage) {
        velumImage = "${options.velumImage}"
    }

    def extraRepo = ""
    if (options.extraRepo) {
        extraRepo = "--extra-repo ${options.extraRepo}"
    }

    timeout(120) {
        dir('automation/caasp-kvm') {
            try {
                withCredentials([
                    string(credentialsId: 'caasp-proxy-host', variable: 'proxy'),
                    string(credentialsId: 'caasp-location', variable: 'location')
                ]) {
                    sh(script: "set -o pipefail; ./caasp-kvm -P ${proxy} -L ${location} ${vanillaFlag} --build ${disableMeltdownSpectreFixesFlag} -m ${masterCount} -w ${workerCount} --image ${options.image} --velum-image ${velumImage} --admin-ram ${options.adminRam} --admin-cpu ${options.adminCpu} --master-ram ${options.masterRam} --master-cpu ${options.masterCpu} --worker-ram ${options.workerRam} --worker-cpu ${options.workerCpu} ${extraRepo} 2>&1 | tee ${WORKSPACE}/logs/caasp-kvm-build.log")
                }
            } finally {
                archiveArtifacts(artifacts: "cluster.tf", fingerprint: true)
                archiveArtifacts(artifacts: "terraform.tfstate", fingerprint: true)

                String timestamp = new Date().format('yMd-hms')
                sh(script: "cp cluster.tf $WORKSPACE/logs/cluster.tf-after_create_deployment-${timestamp}")
                sh(script: "cp terraform.tfstate $WORKSPACE/logs/terraform.tfstate-after_create_deployment-${timestamp}")
            }

            // Read the generated environment file
            environment = new Environment(readJSON(file: 'environment.json'))

            sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            sh(script: "cat ${WORKSPACE}/environment.json")
        }

        archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }

    return environment
}
