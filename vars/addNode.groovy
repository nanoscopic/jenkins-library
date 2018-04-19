// Copyright 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

// addNode:
// bootstrap a new node via velum-interactions
// pre-requisite is one or more nodes ready to accept in velum
// (e.g. via updateEnvironmentCaaspKvm)
Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    timeout(125) {
        try {
            dir('automation/velum-bootstrap') {
                sh(script: './velum-interactions --node-add --environment ${WORKSPACE}/environment.json')
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
