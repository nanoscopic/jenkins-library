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


Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    // TODO: This and configureEnvironment share 90% of the same code

    timeout(90) {
        dir('automation/velum-bootstrap') {
            sh(script: './velum-interactions --setup')
        }
    }

    timeout(125) {
        try {
            dir('automation/velum-bootstrap') {
                sh(script: "./velum-interactions --bootstrap --download-kubeconfig --enable-tiller --environment ${WORKSPACE}/environment.json")
                sh(script: "cp kubeconfig ${WORKSPACE}/kubeconfig")
            }

            // Read the updated environment file
            environment = new Environment(readJSON(file: 'environment.json'))
        } finally {
            dir('automation/velum-bootstrap') {
                junit "velum-bootstrap.xml"
                try {
                    archiveArtifacts(artifacts: "screenshots/**")
                    archiveArtifacts(artifacts: "kubeconfig")
                } catch (Exception exc) {
                    echo "Failed to Archive Artifacts"
                }
            }
        }
    }

    return environment
}
